package com.gitplex.server.rest;

import java.util.Collection;
import java.util.Date;

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
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.rest.jersey.ValidQueryParams;
import com.gitplex.server.security.SecurityUtils;

@Path("/pulls")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestResource {

	private static final String OPEN = "open";
	
	private static final String CLOSED = "closed";
	
	private final PullRequestManager pullRequestManager;
	
	@Inject
	public PullRequestResource(PullRequestManager pullRequestManager) {
		this.pullRequestManager = pullRequestManager;
	}

    @Path("/{pullRequestId}")
    @GET
    public PullRequest get(@PathParam("pullRequestId") Long pullRequestId) {
    	PullRequest request = pullRequestManager.load(pullRequestId);
    	if (!SecurityUtils.canRead(request.getTargetProject()))
    		throw new UnauthorizedException();
    	return request;
    }
        
    @ValidQueryParams
    @GET
    public Response query(
    		@QueryParam("targetProject") String targetProjectId, @QueryParam("targetBranch") String targetBranch,
    		@QueryParam("sourceProject") String sourceProjectId, @QueryParam("sourceBranch") String sourceBranch,
    		@QueryParam("number") Long number, @QueryParam("submitter") String submitterId, 
    		@QueryParam("status") String status, @QueryParam("beginDate") Date beginDate, 
    		@QueryParam("endDate") Date endDate, @QueryParam("per_page") Integer perPage, 
    		@QueryParam("page") Integer page, @Context UriInfo uriInfo) {
    	
    	EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);

    	if (targetProjectId != null)
    		criteria.add(Restrictions.eq("targetProject.id", targetProjectId));
    	if (targetBranch != null)
    		criteria.add(Restrictions.eq("targetBranch", targetBranch));
    		
    	if (sourceProjectId != null)
    		criteria.add(Restrictions.eq("sourceProject.id", targetProjectId));
    	if (sourceBranch != null)
    		criteria.add(Restrictions.eq("sourceBranch", sourceBranch));
		
    	if (number != null)
    		criteria.add(Restrictions.eq("number", number));
    	
		if (OPEN.equalsIgnoreCase(status)) 
			criteria.add(PullRequest.CriterionHelper.ofOpen());
		else if (CLOSED.equalsIgnoreCase(status)) 
			criteria.add(PullRequest.CriterionHelper.ofClosed());
		
		if (submitterId != null)
			criteria.add(Restrictions.eq("submitter.id", submitterId));
		if (beginDate != null)
			criteria.add(Restrictions.ge("submitDate", beginDate));
		if (endDate != null)
			criteria.add(Restrictions.le("submitDate", endDate));

    	if (page == null)
    		page = 1;
    	
    	if (perPage == null || perPage > RestConstants.PAGE_SIZE) 
    		perPage = RestConstants.PAGE_SIZE;

    	int totalCount = pullRequestManager.count(criteria);

    	Collection<PullRequest> requests = pullRequestManager.findRange(criteria, (page-1)*perPage, perPage);
		for (PullRequest request: requests) {
	    	if (!SecurityUtils.canRead(request.getTargetProject())) {
	    		throw new UnauthorizedException("Unable to access pull requests of project '" 
	    				+ request.getTargetProject() + "'");
	    	}
		}

		return Response
				.ok(requests, RestConstants.JSON_UTF8)
				.links(PageUtils.getNavLinks(uriInfo, totalCount, perPage, page))
				.build();
    }
    
}