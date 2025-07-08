package io.onedev.server.rest.resource;

import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.model.IssueLink;
import io.onedev.server.rest.annotation.Api;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.onedev.server.security.SecurityUtils.canAccessIssue;
import static io.onedev.server.security.SecurityUtils.canEditIssueLink;

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
		if (!canEditIssueLink(link.getSource().getProject(), link.getSpec())
				&& !canEditIssueLink(link.getTarget().getProject(), link.getSpec())) {
			throw new UnauthorizedException();
		}
		if (link.getSource().equals(link.getTarget()))
			throw new BadRequestException("Can not link to self");
		if (link.getSpec().getOpposite() != null) {
			if (link.getSource().getTargetLinks().stream().anyMatch(it -> it.getSpec().equals(link.getSpec()) && it.getTarget().equals(link.getTarget())))
				throw new BadRequestException("Source issue already linked to target issue via specified link spec");
			if (!link.getSpec().isMultiple() && link.getSource().getTargetLinks().stream().anyMatch(it -> it.getSpec().equals(link.getSpec())))
				throw new BadRequestException("Link spec is not multiple and the source issue is already linked to another issue via this link spec");
			if (!link.getSpec().getParsedIssueQuery(link.getSource().getProject()).matches(link.getTarget()))
				throw new BadRequestException("Link spec not allowed to link to the target issue");
			if (!link.getSpec().getOpposite().isMultiple() && link.getTarget().getSourceLinks().stream().anyMatch(it -> it.getSpec().equals(link.getSpec())))
				throw new BadRequestException("Opposite side of link spec is not multiple and the target issue is already linked to another issue via this link spec");
			if (!link.getSpec().getOpposite().getParsedIssueQuery(link.getSource().getProject()).matches(link.getSource()))
				throw new BadRequestException("Opposite side of link spec not allowed to link to the source issue");
		} else {
			if (link.getSource().getLinks().stream().anyMatch(it -> it.getSpec().equals(link.getSpec()) && it.getLinked(link.getSource()).equals(link.getTarget()))) 
				throw new BadRequestException("Specified issues already linked via specified link spec");
			if (!link.getSpec().isMultiple() && link.getSource().getLinks().stream().anyMatch(it -> it.getSpec().equals(link.getSpec())))
				throw new BadRequestException("Link spec is not multiple and source issue is already linked to another issue via this link spec");
			var parsedIssueQuery = link.getSpec().getParsedIssueQuery(link.getSource().getProject());
			if (!parsedIssueQuery.matches(link.getSource()) || !parsedIssueQuery.matches(link.getTarget())) 
				throw new BadRequestException("Link spec not allowed to link specified issues");
		}
				
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
