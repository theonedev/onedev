package com.pmease.gitop.core;

import java.io.IOException;

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
import org.eclipse.jgit.http.server.ServletUtils;
import org.eclipse.jgit.transport.PacketLineOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.git.Git;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.permission.ObjectPermission;

@Singleton
public class GitFilter implements Filter {
	
	private static final Logger logger = LoggerFactory.getLogger(GitFilter.class);

	private static final String INFO_REFS = "info/refs";
	
	private final ProjectManager projectManager;
	
	@Inject
	public GitFilter(ProjectManager projectManager) {
		this.projectManager = projectManager;
	}
	
	private Project getProject(HttpServletRequest request, HttpServletResponse response, String pathInfo, String repoInfo) 
			throws IOException {
		
		repoInfo = StringUtils.stripStart(StringUtils.stripEnd(repoInfo, "/"), "/");
		
		String ownerName = StringUtils.substringBefore(repoInfo, "/");
		String projectName = StringUtils.substringAfter(repoInfo, "/");

		if (StringUtils.isBlank(ownerName) || StringUtils.isBlank(projectName)) {
			String url = request.getRequestURL().toString();
			String urlRoot = url.substring(0, url.length()-pathInfo.length());
			String message = "Expecting url of format " + urlRoot + "<owner name>/<project name>";
			logger.error("Error serving git request: " + message);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
			return null;
		} 
		
		if (projectName.endsWith(".git"))
			projectName = projectName.substring(0, projectName.length()-".git".length());
		
		Project project = projectManager.find(ownerName, projectName);
		if (project == null) {
			String message = "Unable to find project '" + projectName + "' owned by '" + ownerName + "'.";
			logger.error("Error serving git request: " + message);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
			return null;
		}
		
		return project;
	}
	
	private void doNotCache(HttpServletResponse response) {
		response.setHeader("Expires", "Fri, 01 Jan 1980 00:00:00 GMT");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
	}
	
	protected void processPacks(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws ServletException, IOException {
		String service = StringUtils.substringAfterLast(pathInfo, "/");

		String repoInfo = StringUtils.substringBeforeLast(pathInfo, "/");
		Project project = getProject(req, resp, pathInfo, repoInfo);
		
		if (project != null) {
			doNotCache(resp);
			resp.setHeader("Content-Type", "application/x-" + service + "-result");			

			Git git = new Git(projectManager.locateStorage(project).ofCode());

			if (service.contains("upload")) {
				if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectRead(project))) {
					throw new UnauthorizedException("You do not have permission to pull from this project.");
				}
				git.upload().input(ServletUtils.getInputStream(req)).output(resp.getOutputStream()).call();
			} else if (service.contains("receive")) {
				if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectWrite(project))) {
					throw new UnauthorizedException("You do not have permission to push to this project.");
				}
				git.receive().input(ServletUtils.getInputStream(req)).output(resp.getOutputStream()).call();
			} else {
				String message = "Invalid service name '" + service + "'.";
				logger.error("Error serving git request: " + message);
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
			}
		}
	}
	
	private void writeInitial(HttpServletResponse resp, String service) throws IOException {
		doNotCache(resp);
		resp.setHeader("Content-Type", "application/x-" + service + "-advertisement");			
		
		PacketLineOut pack = new PacketLineOut(resp.getOutputStream());
		pack.setFlushOnEnd(false);
		pack.writeString("# service=" + service + "\n");
		pack.end();
	}
	
	protected void processRefs(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws ServletException, IOException {
		if (!pathInfo.endsWith(INFO_REFS)) {
			String message = "Invalid refs request url: " + req.getRequestURL();
			logger.error("Error serving git request: " + message);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);			
			return;
		}
		
		String repoInfo = pathInfo.substring(0, pathInfo.length() - INFO_REFS.length());
		Project project = getProject(req, resp, pathInfo, repoInfo);
		if (project != null) {
			String service = req.getParameter("service");
			Git git = new Git(projectManager.locateStorage(project).ofCode());

			if (service.contains("upload")) {
				if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectRead(project))) {
					throw new UnauthorizedException("You do not have permission to pull from this project.");
				}
				writeInitial(resp, service);
				git.advertiseUploadRefs().output(resp.getOutputStream()).call();
			} else if (service.contains("receive")) {
				if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectWrite(project))) {
					throw new UnauthorizedException("You do not have permission to push to this project.");
				}
				writeInitial(resp, service);
				git.advertiseReceiveRefs().output(resp.getOutputStream()).call();
			} else {
				String message = "Invalid service name '" + service + "'.";
				logger.error("Error serving git request: " + message);
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
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
		String pathInfo = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
		pathInfo = StringUtils.stripStart(pathInfo, "/");
		
		if (pathInfo.endsWith(INFO_REFS)) {
			processRefs(httpRequest, httpResponse, pathInfo);
		} else if (pathInfo.endsWith("git-receive-pack") || pathInfo.endsWith("git-upload-pack")) {
			processPacks(httpRequest, httpResponse, pathInfo);
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
	}
	
}
 