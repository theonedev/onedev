package io.onedev.server.git;

import static io.onedev.server.model.Project.decodeFullRepoNameAsPath;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static org.apache.commons.lang3.StringUtils.strip;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.http.server.GitSmartHttpTools;
import org.eclipse.jgit.http.server.ServletUtils;
import org.eclipse.jgit.transport.PacketLineOut;
import org.glassfish.jersey.client.ClientProperties;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.git.command.AdvertiseReceiveRefsCommand;
import io.onedev.server.git.command.AdvertiseUploadRefsCommand;
import io.onedev.server.git.hook.HookUtils;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.CodePullAuthorizationSource;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.OutputStreamWrapper;
import io.onedev.server.util.concurrent.WorkExecutionService;

@Singleton
public class GitFilter implements Filter {
	
	public static final int PACK_PRIORITY = 2;
	
	private static final String INFO_REFS = "info/refs";
	
	private final OneDev onedev;
	
	private final ProjectService projectService;
	
	private final WorkExecutionService workExecutionService;
	
	private final SessionService sessionService;
	
	private final ClusterService clusterService;
	
	private final Set<CodePullAuthorizationSource> codePullAuthorizationSources;
	
	@Inject
	public GitFilter(OneDev oneDev, ProjectService projectService, WorkExecutionService workExecutionService,
                     SessionService sessionService, ClusterService clusterService,
                     Set<CodePullAuthorizationSource> codePullAuthorizationSources) {
		this.onedev = oneDev;
		this.projectService = projectService;
		this.workExecutionService = workExecutionService;
		this.sessionService = sessionService;
		this.clusterService = clusterService;
		this.codePullAuthorizationSources = codePullAuthorizationSources;
	}
	
	private String getPathInfo(HttpServletRequest request) {
		String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
		return StringUtils.stripStart(pathInfo, "/");
	}
	
	private Long getProjectId(String projectPath, boolean clusterAccess, boolean upload) {
		var facade = projectService.findFacadeByPath(projectPath);
		if (facade == null && projectPath.endsWith(".git")) {
			projectPath = StringUtils.substringBeforeLast(projectPath, ".");
			facade = projectService.findFacadeByPath(projectPath);
		}
		if (StringUtils.isBlank(projectPath))
			throw new ExplicitException("Project not specified");
		if (facade == null) 
			reportProjectNotFoundOrInaccessible(projectPath);
		return facade.getId();
	}

	private void reportProjectNotFoundOrInaccessible(String projectPath) {
		if (SecurityUtils.getUser() != null)
			throw new EntityNotFoundException("Project not found or inaccessible: " + projectPath);
		else
			throw new UnauthorizedException("Authentication required");
	}

	private void doNotCache(HttpServletResponse response) {
		response.setHeader("Expires", "Fri, 01 Jan 1980 00:00:00 GMT");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
	}
	
	protected void processPack(final HttpServletRequest request, final HttpServletResponse response) 
			throws IOException, InterruptedException, ExecutionException {
		String principal = (String) SecurityUtils.getSubject().getPrincipal();
		boolean clusterAccess = SecurityUtils.isSystem(principal);
		
		boolean upload = GitSmartHttpTools.isUploadPack(request);
		
		String pathInfo = getPathInfo(request);
		
		String service = StringUtils.substringAfterLast(pathInfo, "/");

		String projectInfo = StringUtils.substringBeforeLast(pathInfo, "/");
		var projectPath = decodeFullRepoNameAsPath(strip(projectInfo, "/"));
		Long projectId = getProjectId(projectPath, clusterAccess, upload);
		
		doNotCache(response);
		response.setHeader("Content-Type", "application/x-" + service + "-result");			

		var hookEnvs = HookUtils.getHookEnvs(projectId, principal);
		
		InputStream stdin = new FilterInputStream(ServletUtils.getInputStream(request)) {

			@Override
			public void close() {
			}
			
		};
		OutputStream stdout = new OutputStreamWrapper(response.getOutputStream()) {
			
			@Override
			public void close() {
			}
			
		};
		
		String protocol = request.getHeader("Git-Protocol");		
		
		if (!clusterAccess) {
			sessionService.openSession();
			try {
				Project project = projectService.load(projectId);
				if (!canAccessProject(request, project))
					reportProjectNotFoundOrInaccessible(projectPath);
				if (upload) {
					checkPullPermission(request, project);
				} else {
					if (!SecurityUtils.canWriteCode(project))
						throw new UnauthorizedException("You do not have permission to push to this project.");
				}			
			} finally {
				sessionService.closeSession();
			}
			
			String activeServer = projectService.getActiveServer(projectId, true);
			if (activeServer.equals(clusterService.getLocalServerAddress())) {
				File gitDir = projectService.getGitDir(projectId);
				if (upload) {
					workExecutionService.submit(PACK_PRIORITY, new Runnable() {
						
						@Override
						public void run() {
							CommandUtils.uploadPack(gitDir, hookEnvs, protocol, stdin, stdout);
						}
						
					}).get();
				} else {
					workExecutionService.submit(PACK_PRIORITY, new Runnable() {
						
						@Override
						public void run() {
							CommandUtils.receivePack(gitDir, hookEnvs, protocol, stdin, stdout);
						}
						
					}).get();
				}
			} else {
				Client client = ClientBuilder.newClient();
				client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				try {
					String serverUrl = clusterService.getServerUrl(activeServer);
					WebTarget target = client.target(serverUrl)
							.path("~api/cluster/git-pack")
							.queryParam("projectId", projectId)
							.queryParam("principal", principal)
							.queryParam("protocol", protocol)
							.queryParam("upload", upload);
					Invocation.Builder builder =  target.request();
					builder.header(HttpHeaders.AUTHORIZATION, 
							KubernetesHelper.BEARER + " " + clusterService.getCredential());
					
					StreamingOutput os = output -> {
						try {
							byte[] buffer = new byte[BUFFER_SIZE];
							int length;
							while ((length = stdin.read(buffer)) > 0) {
								output.write(buffer, 0, length);
								output.flush();
							}
						} finally {
							stdin.close();
						}
					};
					
					try (Response gitResponse = builder.post(Entity.entity(os, MediaType.APPLICATION_OCTET_STREAM))) {
						KubernetesHelper.checkStatus(gitResponse);
						try (InputStream is = gitResponse.readEntity(InputStream.class)) {
							byte[] buffer = new byte[BUFFER_SIZE];
					        int length;
				            while ((length = is.read(buffer)) > 0) {
			            		stdout.write(buffer, 0, length);
			            		stdout.flush();
				            }
						} finally {
							stdout.close();
						}
					}
				} finally {
					client.close();
				}
			}
		} else {
			File gitDir = projectService.getGitDir(projectId);
			if (upload) { 
				// Run immediately if accessed with cluster credential to avoid 
				// possible deadlock as caller itself might also hold some 
				// resources (db connections, work executors etc) 
				CommandUtils.uploadPack(gitDir, hookEnvs, protocol, stdin, stdout);
			} else {
				// Run immediately. See above for reason
				CommandUtils.receivePack(gitDir, hookEnvs, protocol, stdin, stdout);
			}			
		}
	}
	
	private void writeInitial(HttpServletResponse response, String service) throws IOException {
		doNotCache(response);
		response.setHeader("Content-Type", "application/x-" + service + "-advertisement");			
		
		PacketLineOut pack = new PacketLineOut(response.getOutputStream());
		pack.setFlushOnEnd(false);
		pack.writeString("# service=" + service + "\n");
		pack.end();
	}
	
	private void checkPullPermission(HttpServletRequest request, Project project) {
		if (!SecurityUtils.canReadCode(project)) {
			boolean isAuthorized = false;
			for (CodePullAuthorizationSource source: codePullAuthorizationSources) {
				if (source.canPullCode(request, project)) {
					isAuthorized = true;
					break;
				}
			}
			if (!isAuthorized)
				throw new UnauthorizedException("You do not have permission to pull from this project.");
		}
	}

	private boolean canAccessProject(HttpServletRequest request, Project project) {
		if (!SecurityUtils.canAccessProject(project)) {
			for (CodePullAuthorizationSource source: codePullAuthorizationSources) {
				if (source.canPullCode(request, project)) 
					return true;
			}
			return false;
		} else {
			return true;
		}
	}
	
	protected void processRefs(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean clusterAccess = SecurityUtils.isSystem();
		
		String service = request.getParameter("service");
		boolean upload = service.contains("upload");
		
		String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
		pathInfo = StringUtils.stripStart(pathInfo, "/");

		String projectInfo = pathInfo.substring(0, pathInfo.length() - INFO_REFS.length());
		var projectPath = decodeFullRepoNameAsPath(strip(projectInfo, "/"));
		Long projectId = getProjectId(projectPath, clusterAccess, upload);
				
		if (!clusterAccess) {
			sessionService.openSession();
			try {
				Project project = projectService.load(projectId);
				if (!canAccessProject(request, project))
					reportProjectNotFoundOrInaccessible(projectPath);
				if (upload) {
					checkPullPermission(request, project);
					writeInitial(response, service);
				} else {
					if (!SecurityUtils.canWriteCode(project))
						throw new UnauthorizedException("You do not have permission to push to this project.");
					writeInitial(response, service);
				}
			} finally {
				sessionService.closeSession();
			}
		} else { // cluster access, avoid accessing database
			writeInitial(response, service);
		}
		
		OutputStream output = new OutputStreamWrapper(response.getOutputStream()) {

			@Override
			public void close() throws IOException {
			}
			
		};
		
		String protocol = request.getHeader("Git-Protocol");		

		String activeServer = projectService.getActiveServer(projectId, true);
		if (activeServer.equals(clusterService.getLocalServerAddress())) {
			File gitDir = projectService.getGitDir(projectId);
			if (upload) 
				new AdvertiseUploadRefsCommand(gitDir, output).protocol(protocol).run();
			else 
				new AdvertiseReceiveRefsCommand(gitDir, output).protocol(protocol).run();
		} else {
			Client client = ClientBuilder.newClient();
			try {
				String serverUrl = clusterService.getServerUrl(activeServer);
				WebTarget target = client.target(serverUrl)
						.path("~api/cluster/git-advertise-refs")
						.queryParam("projectId", projectId)
						.queryParam("protocol", protocol)
						.queryParam("upload", upload);
				Invocation.Builder builder =  target.request();
				builder.header(HttpHeaders.AUTHORIZATION, 
						KubernetesHelper.BEARER + " " + clusterService.getCredential());
				try (Response gitResponse = builder.get()) {
					KubernetesHelper.checkStatus(gitResponse);
					try (InputStream is = gitResponse.readEntity(InputStream.class)) {
						IOUtils.copy(is, output, BUFFER_SIZE);
					} finally {
						output.close();
					}
				}
			} finally {
				client.close();
			}
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		if (GitSmartHttpTools.isInfoRefs(httpRequest)) {
			if (onedev.isReady())
				processRefs(httpRequest, httpResponse);
			else
				throw new ServerNotReadyException();
		} else if (GitSmartHttpTools.isReceivePack(httpRequest) || GitSmartHttpTools.isUploadPack(httpRequest)) {
			if (onedev.isReady()) {
				try {
					processPack(httpRequest, httpResponse);
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				}
			} else {
				throw new ServerNotReadyException();
			}
		} else {
			chain.doFilter(request, response);
		}
	}
	
	@Override
	public void destroy() {
	}
	
}
 