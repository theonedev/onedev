package com.pmease.gitop.rest.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.permission.ObjectPermission;

@Path("/pull_requests")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public class PullRequestResource {

	private final PullRequestManager pullRequestManager;
	
	@Inject
	public PullRequestResource(PullRequestManager pullRequestManager) {
		this.pullRequestManager = pullRequestManager;
	}
	
    @GET
    @Path("/{pullRequestId}")
    public PullRequest get(@PathParam("pullRequestId") Long pullRequestId) {
    	PullRequest request = pullRequestManager.load(pullRequestId);
    	
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(request.getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	return request;
    }
        
    @GET
    public Collection<PullRequest> query(
    		@Nullable @QueryParam("targetId") Long targetId, 
    		@Nullable @QueryParam("sourceId") Long sourceId, 
    		@Nullable @QueryParam("submitterId") Long submitterId, 
    		@Nullable @QueryParam("status") String status) {

    	List<Criterion> criterions = new ArrayList<>();
		if (targetId != null)
			criterions.add(Restrictions.eq("target.id", targetId));
		if (sourceId != null)
			criterions.add(Restrictions.eq("source.id", sourceId));
		if (submitterId != null)
			criterions.add(Restrictions.eq("submitter.id", submitterId));
		if ("open".equals(status))
			criterions.add(PullRequest.CriterionHelper.ofOpen());
		else if ("closed".equals(status))
			criterions.add(PullRequest.CriterionHelper.ofClosed());
		else
			throw new IllegalArgumentException("status");
		
		List<PullRequest> requests = pullRequestManager.query(criterions.toArray(new Criterion[criterions.size()]));
		
		for (PullRequest request: requests) {
	    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(request.getTarget().getRepository())))
	    		throw new UnauthorizedException("Unauthorized access to pull request " + request.getTarget() + "/" + request.getId());
		}
		return requests;
    }
    
    @POST
    public Long save(@Valid PullRequest pullRequest) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(pullRequest.getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	pullRequestManager.save(pullRequest);
    	
    	return pullRequest.getId();
    }

    @DELETE
    @Path("/{pullRequestId}")
    public void delete(@PathParam("pullRequestId") Long pullRequestId) {
    	PullRequest pullRequest = pullRequestManager.load(pullRequestId);

    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(pullRequest.getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	pullRequestManager.delete(pullRequest);
    }
    
}
