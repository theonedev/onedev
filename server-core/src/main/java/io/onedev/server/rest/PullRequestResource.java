package io.onedev.server.rest;

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

import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.CloseInfo;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.jersey.ValidQueryParams;
import io.onedev.server.security.SecurityUtils;

@Path("/pulls")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestResource {

	private final PullRequestManager pullRequestManager;
	
	@Inject
	public PullRequestResource(PullRequestManager pullRequestManager) {
		this.pullRequestManager = pullRequestManager;
	}

    @Path("/{pullRequestId}")
    @GET
    public PullRequest get(@PathParam("pullRequestId") Long pullRequestId) {
    	PullRequest request = pullRequestManager.load(pullRequestId);
    	if (!SecurityUtils.canReadCode(request.getTargetProject()))
    		throw new UnauthorizedException();
    	return request;
    }
        
    @ValidQueryParams
    @GET
    public Response query(
    		@QueryParam("targetProject") Long targetProjectId, @QueryParam("targetBranch") String targetBranch,
    		@QueryParam("sourceProject") Long sourceProjectId, @QueryParam("sourceBranch") String sourceBranch,
    		@QueryParam("number") Long number, @QueryParam("submitter") String submitterId, 
    		@QueryParam("submittedBefore") Date submittedBefore, @QueryParam("submittedAfter") Date submittedAfter,   
    		@QueryParam("status") String status, @QueryParam("closeUser") Long closeUserId, 
    		@QueryParam("closedBefore") Date closedBefore, @QueryParam("closedAfter") Date closedAfter,   
    		@QueryParam("offset") Integer offset, @QueryParam("count") Integer count, @Context UriInfo uriInfo) {
    	
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

		if (submitterId != null)
			criteria.add(Restrictions.eq("submitter.id", submitterId));
		if (submittedBefore != null)
			criteria.add(Restrictions.le("submitDate", submittedBefore));
		if (submittedAfter != null)
			criteria.add(Restrictions.ge("submitDate", submittedAfter));

		if (status != null) {
	    	if ("OPEN".equals(status))
	    		criteria.add(Restrictions.isNull("closeInfo"));
	    	else
	    		criteria.add(Restrictions.eq("closeInfo.status", CloseInfo.Status.valueOf(status)));
		}
    	if (closeUserId != null)
    		criteria.add(Restrictions.eq("closeInfo.user.id", closeUserId));
		if (closedBefore != null)
			criteria.add(Restrictions.le("closeInfo.date", closedBefore));
		if (closedAfter != null)
			criteria.add(Restrictions.ge("closeInfo.date", closedAfter));
		
    	if (offset == null)
    		offset = 0;
    	
    	if (count == null || count > RestConstants.PAGE_SIZE) 
    		count = RestConstants.PAGE_SIZE;

    	Collection<PullRequest> requests = pullRequestManager.query(criteria, offset, count);
		for (PullRequest request: requests) {
	    	if (!SecurityUtils.canReadCode(request.getTargetProject())) {
	    		throw new UnauthorizedException("Unable to access pull requests of project '" 
	    				+ request.getTargetProject() + "'");
	    	}
		}

		return Response.ok(requests, RestConstants.JSON_UTF8).build();
    }
    
}