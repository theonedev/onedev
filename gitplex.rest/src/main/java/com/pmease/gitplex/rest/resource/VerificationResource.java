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
import com.pmease.gitplex.core.model.Verification;
import com.pmease.gitplex.core.permission.ObjectPermission;

@Path("/verifications")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class VerificationResource {

	private final Dao dao;
	
	@Inject
	public VerificationResource(Dao dao) {
		this.dao = dao;
	}
	
    @GET
    @Path("/{id}")
    public Verification get(@PathParam("id") Long id) {
    	Verification verification  = dao.load(Verification.class, id);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(verification.getRequest().getTarget().getRepository())))
    		throw new UnauthorizedException();
    	return verification;
    }
    
    @ValidQueryParams
    @GET
    public Collection<Verification> query(
    		@QueryParam("request") Long requestId,
    		@QueryParam("configuration") String configuration, 
    		@QueryParam("commit") String commit) {
		EntityCriteria<Verification> criteria = EntityCriteria.of(Verification.class);
		if (requestId != null)
			criteria.add(Restrictions.eq("request.id", requestId));
		if (configuration != null)
			criteria.add(Restrictions.eq("configuration", configuration));
		if (commit != null)
			criteria.add(Restrictions.eq("commit", commit));
		
		List<Verification> verifications = dao.query(criteria);
		
    	for (Verification verification: verifications) {
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
    	Verification verification = dao.load(Verification.class, id);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(verification.getRequest().getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	dao.remove(verification);
    }

    @POST
    public Long save(@NotNull @Valid Verification verification) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(verification.getRequest().getTarget().getRepository())))
    		throw new UnauthorizedException();
    	
    	dao.persist(verification);
    	
    	return verification.getId();
    }
    
}
