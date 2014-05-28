package com.pmease.gitop.rest.resource;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.jersey.ValidQueryParams;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.permission.ObjectPermission;

@Path("/pull_requests")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestResource {

	private final Dao dao;
	
	@Inject
	public PullRequestResource(Dao dao) {
		this.dao = dao;
	}
	
    @GET
    @Path("/{id}")
    public PullRequest get(@PathParam("id") Long id) {
    	PullRequest request = dao.load(PullRequest.class, id);
    	
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(request.getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	return request;
    }
        
    @ValidQueryParams
    @GET
    public Collection<PullRequest> query(
    		@QueryParam("target") Long targetId, @QueryParam("source") Long sourceId, 
    		@QueryParam("submitter") Long submitterId, @QueryParam("status") String status) {
    	EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		if (targetId != null)
			criteria.add(Restrictions.eq("target.id", targetId));
		if (sourceId != null)
			criteria.add(Restrictions.eq("source.id", sourceId));
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
    
    @POST
    public Long save(@NotNull @Valid PullRequest pullRequest) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(pullRequest.getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	dao.persist(pullRequest);
    	
    	return pullRequest.getId();
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
    	PullRequest pullRequest = dao.load(PullRequest.class, id);

    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(pullRequest.getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	dao.remove(pullRequest);
    }
    
}
