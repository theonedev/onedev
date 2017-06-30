package com.gitplex.server.rest.github;

import java.util.ArrayList;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.MergePreview;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.rest.RestConstants;
import com.gitplex.server.security.SecurityUtils;
import com.google.common.base.Preconditions;

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
	
	@Path("/{name}/pulls")
    @GET
    public Response query(
    		@PathParam("name") String projectName, @QueryParam("base") String branch, 
    		@QueryParam("state") String state, @QueryParam("per_page") Integer perPage, 
    		@QueryParam("page") Integer page) {
    	
		Project project = getProject(projectName);

		if (!SecurityUtils.canRead(project))
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
    		page = 0;
    	else
    		page--;
    	
    	if (perPage == null)
    		perPage = RestConstants.PAGE_SIZE;
    	
    	List<Map<String, Object>> entity = new ArrayList<>();
		for (PullRequest request: pullRequestManager.findRange(criteria, page, perPage)) {
			Map<String, Object> map = new HashMap<>();
			map.put("number", String.valueOf(request.getNumber()));
			if (request.isOpen())
				map.put("state", "open");
			else
				map.put("state", "closed");
			map.put("title", request.getTitle());
			MergePreview preview = request.getMergePreview();
			if (preview != null && preview.getMerged() != null) 
				map.put("merge_commit_sha", preview.getMerged());
			
			Map<String, Object> headMap = new HashMap<>();
			if (request.getSourceBranch() != null)
				headMap.put("ref", request.getSourceBranch());
			headMap.put("sha", request.getHeadCommitHash());
			map.put("head", headMap);
			
			Map<String, Object> baseMap = new HashMap<>();
			baseMap.put("ref", request.getTargetBranch());
			baseMap.put("sha", request.getBaseCommitHash());
			map.put("base", baseMap);
			
			entity.add(map);
		}
		return Response.ok(entity, RestConstants.JSON_UTF8).build();
    }
    
}