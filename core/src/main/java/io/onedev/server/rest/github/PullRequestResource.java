package io.onedev.server.rest.github;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;

import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.PageUtils;
import io.onedev.server.rest.RestConstants;
import io.onedev.server.security.SecurityUtils;

@Path("/repos/projects")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
@Singleton
public class PullRequestResource {

	private final ProjectManager projectManager;
	
	private final PullRequestManager pullRequestManager;
	
	@Inject
	public PullRequestResource(ProjectManager projectManager, PullRequestManager pullRequestManager) {
		this.projectManager = projectManager;
		this.pullRequestManager = pullRequestManager;
	}

	private Project getProject(String projectName) {
		return Preconditions.checkNotNull(projectManager.find(projectName));
	}
	
	@Path("/{name}/pulls/{pullRequestNumber}")
	@GET
	public Response getPullRequest(@PathParam("name") String projectName, 
			@PathParam("pullRequestNumber") Long pullRequestNumber, @Context UriInfo uriInfo) {
		Project project = getProject(projectName);

		if (!SecurityUtils.canReadCode(project.getFacade()))
    		throw new UnauthorizedException("Unauthorized access to project '" + projectName + "'");
		
		PullRequest request = Preconditions.checkNotNull(pullRequestManager.find(project, pullRequestNumber));
		
		Map<String, Object> entity = getRequestMap(request, uriInfo);
    	return Response.ok(entity, RestConstants.JSON_UTF8).build();
	}
	
	@Path("/{name}/pulls/{pullRequestNumber}/commits")
	@GET
	public Response getCommits(@PathParam("name") String projectName, 
			@PathParam("pullRequestNumber") Long pullRequestNumber) {
		Project project = getProject(projectName);

		if (!SecurityUtils.canReadCode(project.getFacade()))
    		throw new UnauthorizedException("Unauthorized access to project '" + projectName + "'");
		
		PullRequest request = pullRequestManager.find(project, pullRequestNumber);
		List<Map<String, Object>> entity = new ArrayList<>();
		for (RevCommit commit: request.getCommits()) {
			Map<String, Object> commitMap = new HashMap<>();
			commitMap.put("sha", commit.name());
			
			Map<String, Object> commitDetailMap = new HashMap<>();
			Map<String, String> authorMap = new HashMap<>();
			authorMap.put("name", commit.getAuthorIdent().getName());
			authorMap.put("email", commit.getAuthorIdent().getEmailAddress());
			authorMap.put("date", new SimpleDateFormat(RestConstants.DATE_FORMAT).format(commit.getAuthorIdent().getWhen()));
			commitDetailMap.put("author", authorMap);
			
			Map<String, String> committerMap = new HashMap<>();
			committerMap.put("name", commit.getCommitterIdent().getName());
			committerMap.put("email", commit.getCommitterIdent().getEmailAddress());
			committerMap.put("date", new SimpleDateFormat(RestConstants.DATE_FORMAT).format(commit.getCommitterIdent().getWhen()));
			commitDetailMap.put("committer", authorMap);
			
			commitMap.put("commit", commitDetailMap);
			
			entity.add(commitMap);
		}
    	return Response.ok(entity, RestConstants.JSON_UTF8).build();
	}
	
	private Map<String, Object> getRequestMap(PullRequest request, UriInfo uriInfo) {
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("number", String.valueOf(request.getNumber()));
		if (request.isOpen())
			requestMap.put("state", "open");
		else
			requestMap.put("state", "closed");
		requestMap.put("title", request.getTitle());
		MergePreview preview = request.getMergePreview();
		if (preview != null && preview.getMerged() != null) { 
			requestMap.put("merge_commit_sha", preview.getMerged());
			if (request.isOpen())
				requestMap.put("mergeable", "true");
		} else {
			requestMap.put("mergeable", "false");
		}
		if (request.isMerged()) {
			requestMap.put("merged", "true");
		} else {
			requestMap.put("merged", "false");
		}
		
		Map<String, Object> headMap = new HashMap<>();
		if (request.getSourceBranch() != null) {
			headMap.put("ref", request.getSourceBranch());
			headMap.put("label", request.getSourceBranch());
		}
		headMap.put("sha", request.getHeadCommitHash());
		if (request.getSourceProject() != null) {
			Map<String, Object> repoMap = new HashMap<>();
			repoMap.put("id", "1000000");
			repoMap.put("name", request.getSourceProject().getName());
			headMap.put("repo", repoMap);
		}
		requestMap.put("head", headMap);
		
		Map<String, Object> baseMap = new HashMap<>();
		baseMap.put("ref", request.getTargetBranch());
		baseMap.put("sha", request.getBaseCommitHash());
		Map<String, Object> repoMap = new HashMap<>();
		repoMap.put("id", "1000000");
		repoMap.put("name", request.getTargetProject().getName());
		baseMap.put("repo", repoMap);
		
		requestMap.put("base", baseMap);

		if (request.getSubmitter() != null) {
			Map<String, Object> userMap = new HashMap<>();
			userMap.put("name", request.getSubmitter().getFullName());
			userMap.put("login", request.getSubmitter().getName());
			userMap.put("id", "1000000");
			userMap.put("type", "User");
			userMap.put("site_admin", SecurityUtils.isAdministrator());
			userMap.put("created_at", new SimpleDateFormat(RestConstants.DATE_FORMAT).format(new Date()));
			requestMap.put("user", userMap);
		}
		
		String url = uriInfo.getBaseUriBuilder()
				.path("repos").path("projects")
				.path(request.getTargetProject().getName())
				.path("pulls").path(String.valueOf(request.getNumber()))
				.build().toString();
		requestMap.put("url", url);
		
		SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.DATE_FORMAT);
		requestMap.put("created_at", sdf.format(request.getSortedUpdates().get(0).getDate()));
		requestMap.put("updated_at", sdf.format(request.getLatestUpdate().getDate()));
		
		if (!request.isOpen()) {
			requestMap.put("closed_at", sdf.format(request.getCloseInfo().getDate()));
		}
		
		return requestMap;
	}
	
	@Path("/{name}/pulls")
    @GET
    public Response query(
    		@PathParam("name") String projectName, @QueryParam("base") String branch, 
    		@QueryParam("state") String state, @QueryParam("per_page") Integer perPage, 
    		@QueryParam("page") Integer page, @Context UriInfo uriInfo) {
		Project project = getProject(projectName);

		if (!SecurityUtils.canReadCode(project.getFacade()))
    		throw new UnauthorizedException("Unauthorized access to project '" + projectName + "'");
		
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
    	criteria.add(PullRequest.CriterionHelper.ofTargetProject(project));
    	if ("closed".equals(state))
        	criteria.add(PullRequest.CriterionHelper.ofClosed());
    	else if (state == null || state.equals("open"))
        	criteria.add(PullRequest.CriterionHelper.ofOpen());
    	
    	if (branch != null)
    		criteria.add(Restrictions.eq("targetBranch", branch));
    		
    	if (page == null)
    		page = 1;
    	
    	if (perPage == null || perPage > RestConstants.PAGE_SIZE) 
    		perPage = RestConstants.PAGE_SIZE;

    	int totalCount = pullRequestManager.count(criteria);

    	List<Map<String, Object>> entity = new ArrayList<>();
		for (PullRequest request: pullRequestManager.query(criteria, (page-1)*perPage, perPage)) {
			entity.add(getRequestMap(request, uriInfo));
		}

		return Response
				.ok(entity, RestConstants.JSON_UTF8)
				.links(PageUtils.getNavLinks(uriInfo, totalCount, perPage, page))
				.build();
    }
    
}