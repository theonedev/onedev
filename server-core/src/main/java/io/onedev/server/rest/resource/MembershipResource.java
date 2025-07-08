package io.onedev.server.rest.resource;

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

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.entitymanager.AuditManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.model.Membership;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/memberships")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class MembershipResource {

	private final MembershipManager membershipManager;

	private final AuditManager auditManager;

	@Inject
	public MembershipResource(MembershipManager membershipManager, AuditManager auditManager) {
		this.membershipManager = membershipManager;
		this.auditManager = auditManager;
	}

	@Api(order=100)
	@Path("/{membershipId}")
	@GET
	public Membership getMembership(@PathParam("membershipId") Long membershipId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		return membershipManager.load(membershipId);
	}
	
	@Api(order=200, description="Create new membership")
	@POST
	public Long createMembership(@NotNull Membership membership) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		membershipManager.create(membership);
		var newAuditContent = VersionedXmlDoc.fromBean(membership).toXML();
		auditManager.audit(null, "created membership via RESTful API", null, newAuditContent);
		return membership.getId();
	}
	
	@Api(order=300)
	@Path("/{membershipId}")
	@DELETE
	public Response deleteMembership(@PathParam("membershipId") Long membershipId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		var membership = membershipManager.load(membershipId);
		membershipManager.delete(membership);
		var oldAuditContent = VersionedXmlDoc.fromBean(membership).toXML();
		auditManager.audit(null, "deleted membership via RESTful API", oldAuditContent, null);
		return Response.ok().build();
	}
	
}
