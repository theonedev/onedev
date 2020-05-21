package io.onedev.server.rest;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.jersey.ValidQueryParams;
import io.onedev.server.security.SecurityUtils;

@Path("/builds")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class BuildResource {

	private final BuildManager buildManager;
	
	@Inject
	public BuildResource(BuildManager buildManager) {
		this.buildManager = buildManager;
	}
	
	@ValidQueryParams
	@GET
    public Response query(@QueryParam("job") String jobName, @QueryParam("commit") String commit, 
    		@QueryParam("number") Long number, @QueryParam("offset") Integer offset, 
    		@QueryParam("count") Integer count, @Context UriInfo uriInfo) {
		EntityCriteria<Build> criteria = buildManager.newCriteria();
		if (jobName != null)
			criteria.add(Restrictions.eq("jobName", jobName));
		if (commit != null)
			criteria.add(Restrictions.eq("commit", commit));
		if (number != null)
			criteria.add(Restrictions.eq("number", number));
		
    	if (offset == null)
    		offset = 0;
    	
    	if (count == null || count > RestConstants.PAGE_SIZE) 
    		count = RestConstants.PAGE_SIZE;

    	Collection<Build> builds = buildManager.query(criteria, offset, count);
		for (Build build: builds) {
			if (!SecurityUtils.canAccess(build.getProject()))
				throw new UnauthorizedException("Unable to access project '" + build.getProject().getName() + "'");
		}
		
		return Response.ok(builds, RestConstants.JSON_UTF8).build();
		
    }
    
	@Path("/{buildId}")
    @GET
    public Build get(@PathParam("buildId") Long buildId) {
    	Build build = buildManager.load(buildId);
    	if (!SecurityUtils.canAccess(build.getProject()))
			throw new UnauthorizedException("Unauthorized access to project " + build.getProject().getName());
    	else
    		return build;
    }
	
}
