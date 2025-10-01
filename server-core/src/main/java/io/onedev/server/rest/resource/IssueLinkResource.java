package io.onedev.server.rest.resource;

import static io.onedev.server.security.SecurityUtils.canAccessIssue;
import static io.onedev.server.security.SecurityUtils.canEditIssueLink;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.model.IssueLink;
import io.onedev.server.rest.annotation.Api;

@Path("/issue-links")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueLinkResource {

	private final IssueLinkManager linkManager;

	@Inject
	public IssueLinkResource(IssueLinkManager linkManager) {
		this.linkManager = linkManager;
	}

	@Api(order=100)
	@Path("/{linkId}")
	@GET
	public IssueLink getLink(@PathParam("linkId") Long linkId) {
		var link = linkManager.load(linkId);
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
				
		linkManager.create(link);
		return link.getId();
	}
	
	@Api(order=300)
	@Path("/{linkId}")
	@DELETE
	public Response deleteLink(@PathParam("linkId") Long linkId) {
		var link = linkManager.load(linkId);
		if (!canEditIssueLink(link.getSource().getProject(), link.getSpec()) 
				&& !canEditIssueLink(link.getTarget().getProject(), link.getSpec())) {
			throw new UnauthorizedException();
		}
		linkManager.delete(link);
		return Response.ok().build();
	}
	
}
