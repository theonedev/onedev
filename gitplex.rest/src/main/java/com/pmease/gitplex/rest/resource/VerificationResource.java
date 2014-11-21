package com.pmease.gitplex.rest.resource;

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
import com.pmease.gitplex.core.manager.VerificationManager;
import com.pmease.gitplex.core.model.PullRequestVerification;
import com.pmease.gitplex.core.permission.ObjectPermission;

@Path("/verifications")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class VerificationResource {

	private final Dao dao;
	
	private final VerificationManager verificationManager;
	
	@Inject
	public VerificationResource(Dao dao, VerificationManager verificationManager) {
		this.dao = dao;
		this.verificationManager = verificationManager;
	}
	
    @GET
    @Path("/{id}")
    public PullRequestVerification get(@PathParam("id") Long id) {
    	PullRequestVerification verification  = dao.load(PullRequestVerification.class, id);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(verification.getRequest().getTarget().getRepository())))
    		throw new UnauthorizedException();
    	return verification;
    }
    
    @ValidQueryParams
    @GET
    public Collection<PullRequestVerification> query(
    		@QueryParam("request") Long requestId,
    		@QueryParam("configuration") String configuration, 
    		@QueryParam("commit") String commit) {
		EntityCriteria<PullRequestVerification> criteria = EntityCriteria.of(PullRequestVerification.class);
		if (requestId != null)
			criteria.add(Restrictions.eq("request.id", requestId));
		if (configuration != null)
			criteria.add(Restrictions.eq("configuration", configuration));
		if (commit != null)
			criteria.add(Restrictions.eq("commit", commit));
		
		List<PullRequestVerification> verifications = dao.query(criteria);
		
    	for (PullRequestVerification verification: verifications) {
    		if (!SecurityUtils.getSubject().isPermitted(
    				ObjectPermission.ofRepositoryRead(verification.getRequest().getTarget().getRepository()))) {
    			throw new UnauthorizedException("Unauthorized access to verification " 
    					+ verification.getRequest() + "/" + verification.getId());
    		}
    	}
    	
    	return verifications;
    }
    
    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
    	PullRequestVerification verification = dao.load(PullRequestVerification.class, id);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(verification.getRequest().getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	verificationManager.delete(verification);
    }

    @POST
    public Long save(@NotNull @Valid PullRequestVerification verification) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(verification.getRequest().getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	verificationManager.save(verification);
    	
    	return verification.getId();
    }
    
}
