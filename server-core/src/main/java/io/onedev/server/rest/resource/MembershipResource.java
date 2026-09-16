package io.onedev.server.rest.resource;

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

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.MembershipService;
import io.onedev.server.model.Membership;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/memberships")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class MembershipResource {

	private final MembershipService membershipService;

	private final AuditService auditService;

	@Inject
	public MembershipResource(MembershipService membershipService, AuditService auditService) {
		this.membershipService = membershipService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{membershipId}")
	@GET
	public Membership getMembership(@PathParam("membershipId") Long membershipId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		return membershipService.load(membershipId);
	}
	
	@Api(order=200, description="Create new membership")
	@POST
	public Long createMembership(@NotNull Membership membership) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		membershipService.create(membership);
		var newAuditContent = VersionedXmlDoc.fromBean(membership).toXML();
		auditService.audit(null, "created membership via RESTful API", null, newAuditContent);
		return membership.getId();
	}
	
	@Api(order=300)
	@Path("/{membershipId}")
	@DELETE
	public Response deleteMembership(@PathParam("membershipId") Long membershipId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		var membership = membershipService.load(membershipId);
		membershipService.delete(membership);
		var oldAuditContent = VersionedXmlDoc.fromBean(membership).toXML();
		auditService.audit(null, "deleted membership via RESTful API", oldAuditContent, null);
		return Response.ok().build();
	}
	
}
