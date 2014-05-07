package com.pmease.gitop.rest.resource;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.permission.ObjectPermission;

@Path("/pull_requests")
@Produces(MediaType.APPLICATION_JSON)
public class PullRequestResource {

	private final PullRequestManager pullRequestManager;
	
	private final BranchManager branchManager;
	
	@Inject
	public PullRequestResource(PullRequestManager pullRequestManager, BranchManager branchManager) {
		this.pullRequestManager = pullRequestManager;
		this.branchManager = branchManager;
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
    public Collection<PullRequest> query(@QueryParam("targetBranchId") @Nullable Long targetBranchId) {
    	if (targetBranchId != null) {
    		Branch targetBranch = branchManager.load(targetBranchId); 
        	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(targetBranch.getRepository())))
        		throw new UnauthorizedException();
    		
    		return pullRequestManager.query(
    				PullRequest.CriterionHelper.ofOpen(), 
    				PullRequest.CriterionHelper.ofTarget(targetBranch));
    	} else {
    		Collection<PullRequest> requests = pullRequestManager.query(PullRequest.CriterionHelper.ofOpen());
    		for (PullRequest request: requests) {
    	    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(request.getTarget().getRepository())))
    	    		throw new UnauthorizedException();
    		}
    		return requests;
    	}
    }
}
