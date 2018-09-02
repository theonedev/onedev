package io.onedev.server.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.manager.BuildManager;
import io.onedev.server.model.Build;
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
	
    @GET
    @Path("/{buildId}")
    public Build get(@PathParam("buildId") Long buildId) {
    	Build build = buildManager.load(buildId);
    	if (!SecurityUtils.canReadCode(build.getConfiguration().getProject().getFacade()))
    		throw new UnauthorizedException();
    	return build;
    }
    
    @POST
    public void save(@NotNull(message="may not be empty") @Valid Build build) {
    	if (!SecurityUtils.canWriteCode(build.getConfiguration().getProject().getFacade()))
    		throw new UnauthorizedException();
		
    	buildManager.save(build);
    }
    
}
