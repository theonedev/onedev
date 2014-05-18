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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.jersey.JerseyUtils;
import com.pmease.gitop.model.CommitVerification;
import com.pmease.gitop.model.permission.ObjectPermission;

@Path("/commit_verifications")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class CommitVerificationResource {

	private final Dao dao;
	
	@Inject
	public CommitVerificationResource(Dao dao) {
		this.dao = dao;
	}
	
    @GET
    @Path("/{commitVerificationId}")
    public CommitVerification get(@PathParam("commitVerificationId") Long commitVerificationId) {
    	CommitVerification commitVerification  = dao.load(CommitVerification.class, commitVerificationId);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(commitVerification.getBranch().getRepository())))
    		throw new UnauthorizedException();
    	return commitVerification;
    }
    
	@GET
    public Collection<CommitVerification> query(
    		@QueryParam("branchId") Long branchId,
    		@QueryParam("configuration") String configuration, 
    		@QueryParam("commit") String commit, 
    		@Context UriInfo uriInfo) {
		
    	JerseyUtils.checkQueryParams(uriInfo, "branchId", "configuration", "commit");

		EntityCriteria<CommitVerification> criteria = EntityCriteria.of(CommitVerification.class);
		if (branchId != null)
			criteria.add(Restrictions.eq("branch.id", branchId));
		if (configuration != null)
			criteria.add(Restrictions.eq("configuration", configuration));
		if (commit != null)
			criteria.add(Restrictions.eq("commit", commit));
		
		List<CommitVerification> commitVerifications = dao.query(criteria);
		
    	for (CommitVerification commitVerification: commitVerifications) {
    		if (!SecurityUtils.getSubject().isPermitted(
    				ObjectPermission.ofRepositoryRead(commitVerification.getBranch().getRepository()))) {
    			throw new UnauthorizedException("Unauthorized access to commit verification " 
    					+ commitVerification.getBranch() + "/" + commitVerification.getId());
    		}
    	}
    	
    	return commitVerifications;
    }
    
    @DELETE
    @Path("/{commitVerificationId}")
    public void delete(@PathParam("commitVerificationId") Long commitVerificationId) {
    	CommitVerification commitVerification = dao.load(CommitVerification.class, commitVerificationId);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(commitVerification.getBranch().getRepository())))
    		dao.remove(commitVerification);
    }

    @POST
    public Long save(@NotNull @Valid CommitVerification commitVerification) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(commitVerification.getBranch().getRepository())))
    		dao.persist(commitVerification);
    	
    	return commitVerification.getId();
    }
    
}
