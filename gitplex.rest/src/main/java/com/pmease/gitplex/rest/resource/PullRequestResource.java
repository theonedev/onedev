package com.pmease.gitplex.rest.resource;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.jersey.ValidQueryParams;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.permission.ObjectPermission;

@Path("/pull_requests")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestResource {

	private static final String OPEN = "open";
	
	private static final String CLOSED = "closed";
	
	private final Dao dao;
	
	@Inject
	public PullRequestResource(Dao dao) {
		this.dao = dao;
	}

    @GET
    @Path("/{id}")
    public PullRequest get(@PathParam("id") Long id) {
    	PullRequest request = dao.load(PullRequest.class, id);
    	
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotPull(request.getTargetDepot())))
    		throw new UnauthorizedException();
    	
    	return request;
    }
        
    @ValidQueryParams
    @GET
    public Collection<PullRequest> query(
    		@QueryParam("targetRepoId") Long targetRepoId, @QueryParam("targetBranch") String targetBranch,
    		@QueryParam("sourceRepoId") Long sourceRepoId, @QueryParam("sourceBranch") String sourceBranch, 
    		@QueryParam("submitterId") Long submitterId, @QueryParam("status") String status, 
    		@QueryParam("assigneeId") Long assigneeId, @QueryParam("beginDate") Date beginDate, 
    		@QueryParam("endDate") Date endDate) {
    	
    	EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);

    	if (targetRepoId != null)
    		criteria.add(Restrictions.eq("targetRepo.id", targetRepoId));
    	if (targetBranch != null)
    		criteria.add(Restrictions.eq("targetBranch", targetBranch));
    		
    	if (sourceRepoId != null)
    		criteria.add(Restrictions.eq("sourceRepo.id", targetRepoId));
    	if (sourceBranch != null)
    		criteria.add(Restrictions.eq("sourceBranch", sourceBranch));
		
		if (OPEN.equalsIgnoreCase(status)) 
			criteria.add(PullRequest.CriterionHelper.ofOpen());
		else if (CLOSED.equalsIgnoreCase(status)) 
			criteria.add(PullRequest.CriterionHelper.ofClosed());
		
		if (submitterId != null)
			criteria.add(Restrictions.eq("submitter.id", submitterId));
		if (assigneeId != null)
			criteria.add(Restrictions.eq("assignee.id", assigneeId));
		if (beginDate != null)
			criteria.add(Restrictions.ge("submitDate", beginDate));
		if (endDate != null)
			criteria.add(Restrictions.le("submitDate", endDate));

		List<PullRequest> requests = dao.query(criteria);
		
		for (PullRequest request: requests) {
	    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotPull(request.getTarget().getDepot())))
	    		throw new UnauthorizedException("Unauthorized access to pull request " + request.getTarget() + "/" + request.getId());
		}
		return requests;
    }
    
}