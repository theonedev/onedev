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
import com.pmease.gitplex.core.entity.Verification;
import com.pmease.gitplex.core.manager.VerificationManager;
import com.pmease.gitplex.core.security.ObjectPermission;

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
    public Verification get(@PathParam("id") Long id) {
    	Verification verification  = dao.load(Verification.class, id);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotRead(verification.getRequest().getTargetDepot())))
    		throw new UnauthorizedException();
    	return verification;
    }
    
    @ValidQueryParams
    @GET
    public Collection<Verification> query(
    		@QueryParam("requestId") Long requestId,
    		@QueryParam("configuration") String configuration, 
    		@QueryParam("commit") String commit) {
		EntityCriteria<Verification> criteria = EntityCriteria.of(Verification.class);
		if (requestId != null)
			criteria.add(Restrictions.eq("request.id", requestId));
		if (configuration != null)
			criteria.add(Restrictions.eq("configuration", configuration));
		if (commit != null)
			criteria.add(Restrictions.eq("commit", commit));
		
		List<Verification> verifications = dao.findAll(criteria);
		
    	for (Verification verification: verifications) {
    		if (!SecurityUtils.getSubject().isPermitted(
    				ObjectPermission.ofDepotRead(verification.getRequest().getTargetDepot()))) {
    			throw new UnauthorizedException("Unauthorized access to verification " 
    					+ verification.getRequest() + "/" + verification.getId());
    		}
    	}
    	
    	return verifications;
    }
    
    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
    	Verification verification = dao.load(Verification.class, id);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotWrite(verification.getRequest().getTargetDepot())))
    		throw new UnauthorizedException();
    	
    	verificationManager.delete(verification);
    }

    @POST
    public Long save(@NotNull @Valid Verification verification) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotWrite(verification.getRequest().getTargetDepot())))
    		throw new UnauthorizedException();
    	
    	verificationManager.save(verification);
    	
    	return verification.getId();
    }
    
}
