package io.onedev.server.rest.resource;

import io.onedev.server.entitymanager.PackLabelManager;
import io.onedev.server.model.PackLabel;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(name="Package Label")
@Path("/package-labels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PackLabelResource {

	private final PackLabelManager packLabelManager;

	@Inject
	public PackLabelResource(PackLabelManager packLabelManager) {
		this.packLabelManager = packLabelManager;
	}
	
	@Api(order=200, description="Create package label")
	@POST
	public Long createLabel(@NotNull PackLabel packLabel) {
		if (!SecurityUtils.canWritePack(packLabel.getPack().getProject()))
			throw new UnauthorizedException();
		packLabelManager.create(packLabel);
		return packLabel.getId();
	}
	
	@Api(order=300)
	@Path("/{packLabelId}")
	@DELETE
	public Response deleteLabel(@PathParam("packLabelId") Long packLabelId) {
		PackLabel buildLabel = packLabelManager.load(packLabelId);
		if (!SecurityUtils.canWritePack(buildLabel.getPack().getProject()))
			throw new UnauthorizedException();
		packLabelManager.delete(buildLabel);
		return Response.ok().build();
	}
	
}
