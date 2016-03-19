package com.pmease.gitplex.http;

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
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.http.server.GitSmartHttpTools;
import org.eclipse.jgit.http.server.ServletUtils;
import org.eclipse.jgit.transport.PacketLineOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.git.command.AdvertiseReceiveRefsCommand;
import com.pmease.commons.git.command.AdvertiseUploadRefsCommand;
import com.pmease.commons.git.command.ReceiveCommand;
import com.pmease.commons.git.command.UploadCommand;
import com.pmease.commons.git.exception.GitException;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.WorkManager;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.setting.ServerConfig;

@Singleton
public class GitFilter implements Filter {
	
	private static final Logger logger = LoggerFactory.getLogger(GitFilter.class);

	private static final int PRIORITY = 2;
	
	private static final String INFO_REFS = "info/refs";
	
	private final GitPlex gitPlex;
	
	private final StorageManager storageManager;
	
	private final DepotManager repositoryManager;
	
	private final WorkManager workManager;
	
	private final ServerConfig serverConfig;
	
	@Inject
	public GitFilter(GitPlex gitPlex, StorageManager storageManager, DepotManager repositoryManager, 
			WorkManager workManager, ServerConfig serverConfig) {
		this.gitPlex = gitPlex;
		this.storageManager = storageManager;
		this.repositoryManager = repositoryManager;
		this.workManager = workManager;
		this.serverConfig = serverConfig;
	}
	
	private String getPathInfo(HttpServletRequest request) {
		String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
		return StringUtils.stripStart(pathInfo, "/");
	}
	
	private Depot getDepot(HttpServletRequest request, HttpServletResponse response, String depotInfo) 
			throws IOException {
		depotInfo = StringUtils.stripStart(StringUtils.stripEnd(depotInfo, "/"), "/");
		
		String accountName = StringUtils.substringBefore(depotInfo, "/");
		String repositoryName = StringUtils.substringAfter(depotInfo, "/");

		if (StringUtils.isBlank(accountName) || StringUtils.isBlank(repositoryName)) {
			String url = request.getRequestURL().toString();
			String urlRoot = url.substring(0, url.length()-getPathInfo(request).length());
			throw new GitException(String.format("Expecting url of format %s<account name>/<repository name>", urlRoot));
		} 
		
		if (repositoryName.endsWith(".git"))
			repositoryName = repositoryName.substring(0, repositoryName.length()-".git".length());
		
		Depot depot = repositoryManager.findBy(accountName, repositoryName);
		if (depot == null) {
			throw new GitException(String.format("Unable to find repository %s under account %s.", repositoryName, accountName));
		}
		
		return depot;
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

		String depotInfo = StringUtils.substringBeforeLast(pathInfo, "/");
		Depot depot = getDepot(request, response, depotInfo);
		
		doNotCache(response);
		response.setHeader("Content-Type", "application/x-" + service + "-result");			

		final Map<String, String> environments = new HashMap<>();
		String serverUrl;
        if (serverConfig.getHttpPort() != 0)
            serverUrl = "http://localhost:" + serverConfig.getHttpPort();
        else 
            serverUrl = "https://localhost:" + serverConfig.getSslConfig().getPort();

		environments.put("GITPLEX_URL", serverUrl);
		environments.put("GITPLEX_USER_ID", Account.getCurrentId().toString());
		environments.put("GITPLEX_REPOSITORY_ID", depot.getId().toString());
		final File gitDir = storageManager.getDepotDir(depot);

		if (GitSmartHttpTools.isUploadPack(request)) {
			if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotRead(depot))) {
				throw new UnauthorizedException("You do not have permission to pull from this repository.");
			}
			workManager.submit(new PrioritizedRunnable(PRIORITY) {
				
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
			if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotWrite(depot))) {
				throw new UnauthorizedException("You do not have permission to push to this repository.");
			}
			workManager.submit(new PrioritizedRunnable(PRIORITY) {
				
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

		String depotInfo = pathInfo.substring(0, pathInfo.length() - INFO_REFS.length());
		Depot depot = getDepot(request, response, depotInfo);
		String service = request.getParameter("service");
		
		File gitDir = storageManager.getDepotDir(depot);

		if (service.contains("upload")) {
			if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotRead(depot))) {
				throw new UnauthorizedException("You do not have permission to pull from this repository.");
			}
			writeInitial(response, service);
			new AdvertiseUploadRefsCommand(gitDir).output(response.getOutputStream()).call();
		} else {
			if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotWrite(depot))) {
				throw new UnauthorizedException("You do not have permission to push to this repository.");
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
 