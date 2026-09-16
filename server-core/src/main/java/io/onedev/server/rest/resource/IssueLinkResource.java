package io.onedev.server.rest.resource;

import static io.onedev.server.security.SecurityUtils.canAccessIssue;
import static io.onedev.server.security.SecurityUtils.canEditIssueLink;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.service.IssueLinkService;
import io.onedev.server.model.IssueLink;
import io.onedev.server.rest.annotation.Api;

@Path("/issue-links")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueLinkResource {

	private final IssueLinkService linkService;

	@Inject
	public IssueLinkResource(IssueLinkService linkService) {
		this.linkService = linkService;
	}

	@Api(order=100)
	@Path("/{linkId}")
	@GET
	public IssueLink getLink(@PathParam("linkId") Long linkId) {
		var link = linkService.load(linkId);
		if (!canAccessIssue(link.getTarget()) && !canAccessIssue(link.getSource()))
			throw new UnauthorizedException();
		return link;
	}
	
	@Api(order=200, description="Create new issue link")
	@POST
	public Long createLink(@NotNull IssueLink link) {
		if (!canAccessIssue(link.getSource()) || !canAccessIssue(link.getTarget()))
			throw new UnauthorizedException("No permission to access specified issues");
		if (!canEditIssueLink(link.getSource().getProject(), link.getSpec())
				|| !canEditIssueLink(link.getTarget().getProject(), link.getSpec())) {
			throw new UnauthorizedException("No permission to add specified link for specified issues");

		}
		link.validate();
						
		linkService.create(link);
		return link.getId();
	}
	
	@Api(order=300)
	@Path("/{linkId}")
	@DELETE
	public Response deleteLink(@PathParam("linkId") Long linkId) {
		var link = linkService.load(linkId);
		if (!canEditIssueLink(link.getSource().getProject(), link.getSpec()) 
				&& !canEditIssueLink(link.getTarget().getProject(), link.getSpec())) {
			throw new UnauthorizedException();
		}
		linkService.delete(link);
		return Response.ok().build();
	}
	
}
