package com.gitplex.server.git;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.http.server.GitSmartHttpTools;
import org.eclipse.jgit.http.server.ServletUtils;
import org.eclipse.jgit.transport.PacketLineOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.server.GitPlex;
import com.gitplex.server.git.command.AdvertiseReceiveRefsCommand;
import com.gitplex.server.git.command.AdvertiseUploadRefsCommand;
import com.gitplex.server.git.command.ReceiveCommand;
import com.gitplex.server.git.command.UploadCommand;
import com.gitplex.server.git.exception.GitException;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.manager.WorkExecutor;
import com.gitplex.server.model.User;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.model.Project;
import com.gitplex.server.util.concurrent.PrioritizedRunnable;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.serverconfig.ServerConfig;

@Singleton
public class GitFilter implements Filter {
	
	private static final Logger logger = LoggerFactory.getLogger(GitFilter.class);

	private static final int PRIORITY = 2;
	
	private static final String INFO_REFS = "info/refs";
	
	private final GitPlex gitPlex;
	
	private final StorageManager storageManager;
	
	private final ProjectManager projectManager;
	
	private final WorkExecutor workExecutor;
	
	private final ServerConfig serverConfig;
	
	private final ConfigManager configManager;
	
	@Inject
	public GitFilter(GitPlex gitPlex, StorageManager storageManager, ProjectManager projectManager, 
			WorkExecutor workManager, ServerConfig serverConfig, ConfigManager configManager) {
		this.gitPlex = gitPlex;
		this.storageManager = storageManager;
		this.projectManager = projectManager;
		this.workExecutor = workManager;
		this.serverConfig = serverConfig;
		this.configManager = configManager;
	}
	
	private String getPathInfo(HttpServletRequest request) {
		String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
		return StringUtils.stripStart(pathInfo, "/");
	}
	
	@Sessional
	protected ProjectFacade getProject(HttpServletRequest request, HttpServletResponse response, String projectInfo) 
			throws IOException {
		projectInfo = StringUtils.stripStart(StringUtils.stripEnd(projectInfo, "/"), "/");

		if (StringUtils.isBlank(projectInfo) || !projectInfo.startsWith("projects/")) {
			String url = request.getRequestURL().toString();
			String urlRoot = url.substring(0, url.length()-getPathInfo(request).length());
			throw new GitException(String.format("Expecting url of format %sprojects/<project name>", urlRoot));
		} 
		
		String projectName = StringUtils.substringAfter(projectInfo, "/");
		
		if (projectName.endsWith(".git"))
			projectName = projectName.substring(0, projectName.length()-".git".length());
		
		Project project = projectManager.find(projectName);
		if (project == null) {
			throw new GitException(String.format("Unable to find project %s", projectName));
		}
		
		return project.getFacade();
	}
	
	private void doNotCache(HttpServletResponse response) {
		response.setHeader("Expires", "Fri, 01 Jan 1980 00:00:00 GMT");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
	}
	
	protected void processPacks(final HttpServletRequest request, final HttpServletResponse response) 
			throws ServletException, IOException, InterruptedException, ExecutionException {
		String pathInfo = getPathInfo(request);
		
		String service = StringUtils.substringAfterLast(pathInfo, "/");

		String projectInfo = StringUtils.substringBeforeLast(pathInfo, "/");
		ProjectFacade project = getProject(request, response, projectInfo);
		
		doNotCache(response);
		response.setHeader("Content-Type", "application/x-" + service + "-result");			

		final Map<String, String> environments = new HashMap<>();
		String serverUrl;
        if (serverConfig.getHttpPort() != 0)
            serverUrl = "http://localhost:" + serverConfig.getHttpPort();
        else 
            serverUrl = "https://localhost:" + serverConfig.getSslConfig().getPort();

        environments.put("GITPLEX_CURL", configManager.getSystemSetting().getCurlConfig().getExecutable());
		environments.put("GITPLEX_URL", serverUrl);
		environments.put("GITPLEX_USER_ID", User.getCurrentId().toString());
		environments.put("GITPLEX_REPOSITORY_ID", project.getId().toString());
		File gitDir = storageManager.getProjectGitDir(project.getId());

		if (GitSmartHttpTools.isUploadPack(request)) {
			if (!SecurityUtils.canRead(project))
				throw new UnauthorizedException("You do not have permission to pull from this project.");
			workExecutor.submit(new PrioritizedRunnable(PRIORITY) {
				
				@Override
				public void run() {
					try {
						new UploadCommand(gitDir, environments)
								.input(ServletUtils.getInputStream(request))
								.output(response.getOutputStream())
								.call();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				
			}).get();
		} else {
			if (!SecurityUtils.canWrite(project)) {
				throw new UnauthorizedException("You do not have permission to push to this project.");
			}
			workExecutor.submit(new PrioritizedRunnable(PRIORITY) {
				
				@Override
				public void run() {
					try {
						new ReceiveCommand(gitDir, environments)
								.input(ServletUtils.getInputStream(request))
								.output(response.getOutputStream())
								.call();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				
			}).get();
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
	
	protected void processRefs(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
		pathInfo = StringUtils.stripStart(pathInfo, "/");

		String projectInfo = pathInfo.substring(0, pathInfo.length() - INFO_REFS.length());
		ProjectFacade project = getProject(request, response, projectInfo);
		String service = request.getParameter("service");
		
		File gitDir = storageManager.getProjectGitDir(project.getId());

		if (service.contains("upload")) {
			if (!SecurityUtils.canRead(project)) 
				throw new UnauthorizedException("You do not have permission to pull from this project.");
			writeInitial(response, service);
			new AdvertiseUploadRefsCommand(gitDir).output(response.getOutputStream()).call();
		} else {
			if (!SecurityUtils.canWrite(project)) {
				throw new UnauthorizedException("You do not have permission to push to this project.");
			}
			writeInitial(response, service);
			new AdvertiseReceiveRefsCommand(gitDir).output(response.getOutputStream()).call();
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

		try {
			if (GitSmartHttpTools.isInfoRefs(httpRequest)) {
				if (gitPlex.isReady())
					processRefs(httpRequest, httpResponse);
				else
					throw new GitException("Server is not ready");
			} else if (GitSmartHttpTools.isReceivePack(httpRequest) || GitSmartHttpTools.isUploadPack(httpRequest)) {
				if (gitPlex.isReady())
					processPacks(httpRequest, httpResponse);
				else
					throw new GitException("Server is not ready");
			} else {
				chain.doFilter(request, response);
			}
		} catch (GitException|InterruptedException|ExecutionException e) {
			logger.error("Error serving git request", e);
			GitSmartHttpTools.sendError(httpRequest, httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	@Override
	public void destroy() {
	}
	
}
 