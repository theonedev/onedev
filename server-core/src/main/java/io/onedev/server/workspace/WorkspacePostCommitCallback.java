package io.onedev.server.workspace;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import io.onedev.commons.utils.StringUtils;

@Singleton
public class WorkspacePostCommitCallback extends HttpServlet {

	public static final String PATH = "/git-workspace-postcommit-callback";

	private final WorkspaceService workspaceService;

	@Inject
	public WorkspacePostCommitCallback(WorkspaceService workspaceService) {
		this.workspaceService = workspaceService;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/plain");		
		List<String> fields = StringUtils.splitAndTrim(request.getPathInfo(), "/");
		if (fields.size() != 1) 
			throw new BadRequestException("Invalid request path");

		String workspaceToken = fields.get(0);

		var context = workspaceService.getWorkspaceContext(workspaceToken, false);
		if (context == null) 
			throw new NotFoundException("No workspace found with specified token");

		workspaceService.onPostCommit(context);
		response.setStatus(HttpServletResponse.SC_OK);
	}

}
