package com.pmease.gitplex.rest.resource;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.IntegrationPreview;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.permission.ObjectPermission;

@Path("/pull_requests")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestResource {

	private final Dao dao;
	
	private final BranchManager branchManager;
	
	private final PullRequestManager pullRequestManager;
	
	@Inject
	public PullRequestResource(Dao dao, BranchManager branchManager, PullRequestManager pullRequestManager) {
		this.dao = dao;
		this.branchManager = branchManager;
		this.pullRequestManager = pullRequestManager;
	}

    @GET
    @Path("/{id}")
    public PullRequest get(@PathParam("id") Long id) {
    	PullRequest request = dao.load(PullRequest.class, id);
    	
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(request.getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	return request;
    }
        
    @GET
    @Path("/{id}/latest_update")
    public PullRequestUpdate getLatestUpdate(@PathParam("id") Long id) {
    	PullRequest request = dao.load(PullRequest.class, id);
    	
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(request.getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	return request.getLatestUpdate();
    }

    @GET
    @Path("/{id}/integrated_commit")
    public String getIntegratedCommit(@PathParam("id") Long id) {
    	PullRequest request = dao.load(PullRequest.class, id);
    	
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(request.getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	IntegrationPreview preview = pullRequestManager.previewIntegration(request);
    	if (preview != null)
    		return preview.getIntegrated();
    	else
    		return null;
    }

    @ValidQueryParams
    @GET
    public Collection<PullRequest> query(
    		@QueryParam("target") Long targetId, @QueryParam("targetPath") String targetPath, 
    		@QueryParam("source") Long sourceId, @QueryParam("sourcePath") String sourcePath, 
    		@QueryParam("submitter") Long submitterId, @QueryParam("status") String status) {
    	EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);

    	if (targetId != null) {
			criteria.add(Restrictions.eq("target.id", targetId));
		} else if (targetPath != null) {
			Branch target = branchManager.findBy(targetPath);
			if (target != null)
				criteria.add(Restrictions.eq("target.id", target.getId()));
			else
				throw new NotFoundException("Unable to find target branch: " + targetPath);
		}

    	if (sourceId != null) {
			criteria.add(Restrictions.eq("source.id", sourceId));
		} else if (sourcePath != null) {
			Branch source = branchManager.findBy(sourcePath);
			if (source != null)
				criteria.add(Restrictions.eq("source.id", source.getId()));
			else
				throw new NotFoundException("Unable to find source branch: " + targetPath);
		}
    	
		if (submitterId != null)
			criteria.add(Restrictions.eq("submitter.id", submitterId));
		if (status != null) {
			if (status.equals("open"))
				criteria.add(PullRequest.CriterionHelper.ofOpen());
			else if (status.equals("closed"))
				criteria.add(PullRequest.CriterionHelper.ofClosed());
			else
				throw new IllegalArgumentException("status");
		}
		
		List<PullRequest> requests = dao.query(criteria);
		
		for (PullRequest request: requests) {
	    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(request.getTarget().getRepository())))
	    		throw new UnauthorizedException("Unauthorized access to pull request " + request.getTarget() + "/" + request.getId());
		}
		return requests;
    }
    
}
