package io.onedev.server.rest;

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

import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.model.Membership;
import io.onedev.server.security.SecurityUtils;

@Path("/memberships")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class MembershipResource {

	private final MembershipManager membershipManager;

	@Inject
	public MembershipResource(MembershipManager membershipManager) {
		this.membershipManager = membershipManager;
	}

	@Path("/{membershipId}")
	@GET
	public Membership get(@PathParam("membershipId") Long membershipId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		return membershipManager.load(membershipId);
	}
	
	@POST
	public Long save(@NotNull Membership membership) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		membershipManager.save(membership);
		return membership.getId();
	}
	
	@Path("/{membershipId}")
	@DELETE
	public Response delete(@PathParam("membershipId") Long membershipId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		membershipManager.delete(membershipManager.load(membershipId));
		return Response.ok().build();
	}
	
}
