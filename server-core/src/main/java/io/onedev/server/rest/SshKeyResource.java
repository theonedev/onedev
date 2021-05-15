package io.onedev.server.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.security.SecurityUtils;

@Path("/ssh-keys")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class SshKeyResource {

	private final SshKeyManager sshKeyManager;

	@Inject
	public SshKeyResource(SshKeyManager sshKeyManager) {
		this.sshKeyManager = sshKeyManager;
	}

	@Path("/{sshKeyId}")
	@GET
	public SshKey get(@PathParam("sshKeyId") Long sshKeyId) {
		SshKey sshKey = sshKeyManager.load(sshKeyId);
    	if (!SecurityUtils.isAdministrator() && !sshKey.getOwner().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return sshKey;
	}
	
	@Path("/{sshKeyId}")
	@DELETE
	public Response delete(@PathParam("sshKeyId") Long sshKeyId) {
		SshKey sshKey = sshKeyManager.load(sshKeyId);
    	if (!SecurityUtils.isAdministrator() && !sshKey.getOwner().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		sshKeyManager.delete(sshKey);
		return Response.ok().build();
	}
	
}
