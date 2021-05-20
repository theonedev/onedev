package io.onedev.server.rest;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildParam;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.jersey.InvalidParamException;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.security.SecurityUtils;

@Api(order=4000)
@Path("/builds")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class BuildResource {

	private final BuildManager buildManager;
	
	@Inject
	public BuildResource(BuildManager buildManager) {
		this.buildManager = buildManager;
	}

	@Api(order=100)
	@Path("/{buildId}")
    @GET
    public Build get(@PathParam("buildId") Long buildId) {
		Build build = buildManager.load(buildId);
    	if (!SecurityUtils.canAccess(build)) 
			throw new UnauthorizedException();
    	return build;
    }

	@Api(order=200)
	@Path("/{buildId}/params")
    @GET
    public Collection<BuildParam> getParams(@PathParam("buildId") Long buildId) {
		Build build = buildManager.load(buildId);
    	if (!SecurityUtils.canAccess(build)) 
			throw new UnauthorizedException();
    	return build.getParams();
    }
	
	@Api(order=300)
	@Path("/{buildId}/dependencies")
    @GET
    public Collection<BuildDependence> getDependencies(@PathParam("buildId") Long buildId) {
		Build build = buildManager.load(buildId);
    	if (!SecurityUtils.canAccess(build)) 
			throw new UnauthorizedException();
    	return build.getDependencies();
    }
	
	@Api(order=400)
	@Path("/{buildId}/dependents")
    @GET
    public Collection<BuildDependence> getDependents(@PathParam("buildId") Long buildId) {
		Build build = buildManager.load(buildId);
    	if (!SecurityUtils.canAccess(build)) 
			throw new UnauthorizedException();
    	return build.getDependents();
    }
	
	@Api(order=500)
	@Path("/{buildId}/fixed-issue-numbers")
    @GET
    public Collection<Long> getFixedIssueNumbers(@PathParam("buildId") Long buildId) {
		Build build = buildManager.load(buildId);
    	if (!SecurityUtils.canAccess(build)) 
			throw new UnauthorizedException();
    	return build.getFixedIssueNumbers();
    }
	
	@Api(order=600)
	@GET
    public List<Build> query(@QueryParam("query") String query, @QueryParam("offset") int offset, 
    		@QueryParam("count") int count) {
		
    	if (count > RestUtils.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should be less than " + RestUtils.MAX_PAGE_SIZE);

    	BuildQuery parsedQuery;
		try {
			parsedQuery = BuildQuery.parse(null, query, true, true);
		} catch (Exception e) {
			throw new InvalidParamException("Error parsing query", e);
		}
    	
    	return buildManager.query(null, parsedQuery, offset, count);
    }
	
	@Api(order=700)
	@Path("/{buildId}")
    @DELETE
    public Response delete(@PathParam("buildId") Long buildId) {
    	Build build = buildManager.load(buildId);
    	if (!SecurityUtils.canManage(build))
			throw new UnauthorizedException();
    	buildManager.delete(build);
    	return Response.ok().build();
    }
	
}
