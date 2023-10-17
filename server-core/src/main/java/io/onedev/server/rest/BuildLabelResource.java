package io.onedev.server.rest;

import io.onedev.server.entitymanager.BuildLabelManager;
import io.onedev.server.model.BuildLabel;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(order=10400)
@Path("/build-labels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class BuildLabelResource {

	private final BuildLabelManager buildLabelManager;

	@Inject
	public BuildLabelResource(BuildLabelManager buildLabelManager) {
		this.buildLabelManager = buildLabelManager;
	}
	
	@Api(order=200, description="Create build label")
	@POST
	public Long create(@NotNull BuildLabel buildLabel) {
		if (!SecurityUtils.canManage(buildLabel.getBuild()))
			throw new UnauthorizedException();
		buildLabelManager.create(buildLabel);
		return buildLabel.getId();
	}
	
	@Api(order=300)
	@Path("/{buildLabelId}")
	@DELETE
	public Response delete(@PathParam("buildLabelId") Long buildLabelId) {
		BuildLabel buildLabel = buildLabelManager.load(buildLabelId);
		if (!SecurityUtils.canManage(buildLabel.getBuild()))
			throw new UnauthorizedException();
		buildLabelManager.delete(buildLabel);
		return Response.ok().build();
	}
	
}
