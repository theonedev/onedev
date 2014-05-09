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

import com.pmease.gitop.core.manager.BuildResultManager;
import com.pmease.gitop.model.BuildResult;
import com.pmease.gitop.model.permission.ObjectPermission;

@Path("/build_results")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BuildResultResource {

	private final BuildResultManager buildResultManager;
	
	@Inject
	public BuildResultResource(BuildResultManager buildResultManager) {
		this.buildResultManager = buildResultManager;
	}
	
    @GET
    @Path("/{buildResultId}")
    public BuildResult get(@PathParam("buildResultId") Long buildResultId) {
    	BuildResult buildResult  = buildResultManager.load(buildResultId);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(buildResult.getBranch().getRepository())))
    		throw new UnauthorizedException();
    	return buildResult;
    }
    
	@GET
    public Collection<BuildResult> query(
    		@Nullable @QueryParam("branchId") Long branchId,
    		@Nullable @QueryParam("configuration") String configuration, 
    		@Nullable @QueryParam("commit") String commit) {

		List<Criterion> criterions = new ArrayList<>();
		if (branchId != null)
			criterions.add(Restrictions.eq("branch.id", branchId));
		if (configuration != null)
			criterions.add(Restrictions.eq("configuration", configuration));
		if (commit != null)
			criterions.add(Restrictions.eq("commit", commit));
		
		List<BuildResult> buildResults = buildResultManager.query(criterions.toArray(new Criterion[criterions.size()]));
		
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
    	BuildResult buildResult = buildResultManager.load(buildResultId);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(buildResult.getBranch().getRepository())))
    		buildResultManager.delete(buildResult);
    }

    @POST
    public Long save(@Valid BuildResult buildResult) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(buildResult.getBranch().getRepository())))
    		buildResultManager.save(buildResult);
    	
    	return buildResult.getId();
    }
    
}
