package io.onedev.server.rest;

import io.onedev.server.entitymanager.PullRequestLabelManager;
import io.onedev.server.model.PullRequestLabel;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(order=10500)
@Path("/pull-request-labels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestLabelResource {

	private final PullRequestLabelManager pullRequestLabelManager;

	@Inject
	public PullRequestLabelResource(PullRequestLabelManager pullRequestLabelManager) {
		this.pullRequestLabelManager = pullRequestLabelManager;
	}
	
	@Api(order=200, description="Create pull request label")
	@POST
	public Long create(@NotNull PullRequestLabel pullRequestLabel) {
		if (!SecurityUtils.canModify(pullRequestLabel.getRequest()))
			throw new UnauthorizedException();
		pullRequestLabelManager.create(pullRequestLabel);
		return pullRequestLabel.getId();
	}
	
	@Api(order=300)
	@Path("/{pullRequestLabelId}")
	@DELETE
	public Response delete(@PathParam("pullRequestLabelId") Long pullRequestLabelId) {
		PullRequestLabel pullRequestLabel = pullRequestLabelManager.load(pullRequestLabelId);
		if (!SecurityUtils.canModify(pullRequestLabel.getRequest()))
			throw new UnauthorizedException();
		pullRequestLabelManager.delete(pullRequestLabel);
		return Response.ok().build();
	}
	
}
