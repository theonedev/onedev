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
import com.pmease.gitop.model.BuildResult;
import com.pmease.gitop.model.permission.ObjectPermission;

@Path("/build_results")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class BuildResultResource {

	private final Dao dao;
	
	@Inject
	public BuildResultResource(Dao dao) {
		this.dao = dao;
	}
	
    @GET
    @Path("/{buildResultId}")
    public BuildResult get(@PathParam("buildResultId") Long buildResultId) {
    	BuildResult buildResult  = dao.load(BuildResult.class, buildResultId);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(buildResult.getBranch().getRepository())))
    		throw new UnauthorizedException();
    	return buildResult;
    }
    
	@GET
    public Collection<BuildResult> query(
    		@QueryParam("branchId") Long branchId,
    		@QueryParam("configuration") String configuration, 
    		@QueryParam("commit") String commit, 
    		@Context UriInfo uriInfo) {
		
    	JerseyUtils.checkQueryParams(uriInfo, "branchId", "configuration", "commit");

		EntityCriteria<BuildResult> criteria = EntityCriteria.of(BuildResult.class);
		if (branchId != null)
			criteria.add(Restrictions.eq("branch.id", branchId));
		if (configuration != null)
			criteria.add(Restrictions.eq("configuration", configuration));
		if (commit != null)
			criteria.add(Restrictions.eq("commit", commit));
		
		List<BuildResult> buildResults = dao.query(criteria);
		
    	for (BuildResult buildResult: buildResults) {
    		if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(buildResult.getBranch().getRepository()))) {
    			throw new UnauthorizedException("Unauthorized access to build result " + buildResult.getBranch() + "/" + buildResult.getId());
    		}
    	}
    	
    	return buildResults;
    }
    
    @DELETE
    @Path("/{buildResultId}")
    public void delete(@PathParam("buildResultId") Long buildResultId) {
    	BuildResult buildResult = dao.load(BuildResult.class, buildResultId);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(buildResult.getBranch().getRepository())))
    		dao.remove(buildResult);
    }

    @POST
    public Long save(@NotNull @Valid BuildResult buildResult) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(buildResult.getBranch().getRepository())))
    		dao.persist(buildResult);
    	
    	return buildResult.getId();
    }
    
}
