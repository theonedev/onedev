package com.pmease.gitop.core;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.http.server.ServletUtils;
import org.eclipse.jgit.transport.PacketLineOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.git.Git;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.core.model.Repository;

@Singleton
@SuppressWarnings("serial")
public class GitServlet extends HttpServlet {
	
	private static final Logger logger = LoggerFactory.getLogger(GitServlet.class);

	private static final String INFO_REFS = "/info/refs";
	
	private final RepositoryManager repositoryManager;
	
	@Inject
	public GitServlet(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}
	
	private Repository getRepository(HttpServletRequest request, HttpServletResponse response, String repoInfo) throws IOException {
		String ownerName = StringUtils.substringBefore(repoInfo, "/");
		String repositoryName = StringUtils.substringAfter(repoInfo, "/");

		if (StringUtils.isBlank(ownerName) || StringUtils.isBlank(repositoryName)) {
			String urlRoot = StringUtils.substringBeforeLast(request.getRequestURL().toString(), "?");
			urlRoot = StringUtils.substringBeforeLast(urlRoot, "/");
			String message = "Expecting url of format " + urlRoot + "/<owner name>/<repository name>";
			logger.error("Error serving git request: " + message);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
			return null;
		} 
		
		Repository repository = repositoryManager.find(ownerName, repositoryName);
		if (repository == null) {
			String message = "Unable to find repository '" + repositoryName + "' owned by '" + ownerName + "'.";
			logger.error("Error serving git request: " + message);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
			return null;
		}
		
		return repository;
	}
	
	private void doNotCache(HttpServletResponse response) {
		response.setHeader("Expires", "Fri, 01 Jan 1980 00:00:00 GMT");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = StringUtils.stripStart(req.getPathInfo(), "/");
		String service = StringUtils.substringAfterLast(pathInfo, "/");

		String repoInfo = StringUtils.substringBeforeLast(pathInfo, "/");
		Repository repository = getRepository(req, resp, repoInfo);
		
		if (repository != null) {
			doNotCache(resp);
			resp.setHeader("Content-Type", "application/x-" + service + "-result");			

			Git git = new Git(repositoryManager.locateStorage(repository));

			try {
				if (service.contains("upload")) {
					git.upload().input(ServletUtils.getInputStream(req)).output(resp.getOutputStream()).call();
				} else if (service.contains("receive")) {
					git.receive().input(ServletUtils.getInputStream(req)).output(resp.getOutputStream()).call();
				} else {
					String message = "Invalid service name '" + service + "'.";
					logger.error("Error serving git request: " + message);
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
				}
			} catch (Exception e) {
				logger.error("Error serving git request.", e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = StringUtils.stripStart(req.getPathInfo(), "/");
		
		if (!pathInfo.endsWith(INFO_REFS)) {
			String message = "Invalid refs request url: " + req.getRequestURL();
			logger.error("Error serving git request: " + message);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);			
			return;
		}
		
		String repoInfo = pathInfo.substring(0, pathInfo.length() - INFO_REFS.length());
		Repository repository = getRepository(req, resp, repoInfo);
		if (repository != null) {
			doNotCache(resp);
			String service = req.getParameter("service");
			resp.setHeader("Content-Type", "application/x-" + service + "-advertisement");			
			
			PacketLineOut pack = new PacketLineOut(resp.getOutputStream());
			pack.setFlushOnEnd(false);
			pack.writeString("# service=" + service + "\n");
			pack.end();
			
			Git git = new Git(repositoryManager.locateStorage(repository));

			try {
				if (service.contains("upload")) {
					git.advertiseUploadRefs().output(resp.getOutputStream()).call();
				} else if (service.contains("receive")) {
					git.advertiseReceiveRefs().output(resp.getOutputStream()).call();
				} else {
					String message = "Invalid service name '" + service + "'.";
					logger.error("Error serving git request: " + message);
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
				}
			} catch (Exception e) {
				logger.error("Error serving git request.", e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}
		}
	}
	
}
