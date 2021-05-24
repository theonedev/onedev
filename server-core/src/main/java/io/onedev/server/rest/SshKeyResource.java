package io.onedev.server.rest;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;
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

import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Api(order=5100)
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

	@Api(order=100)
	@Path("/{sshKeyId}")
	@GET
	public SshKey get(@PathParam("sshKeyId") Long sshKeyId) {
		SshKey sshKey = sshKeyManager.load(sshKeyId);
    	if (!SecurityUtils.isAdministrator() && !sshKey.getOwner().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return sshKey;
	}
	
	@Api(order=150, description="Update ssh key of specified id in request body, or create new if id property not provided")
	@POST
	public Long createOrUpdate(SshKey sshKey) {
    	if (!SecurityUtils.isAdministrator() && !sshKey.getOwner().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	if (sshKey.isNew())
    		sshKey.setCreatedAt(new Date());
    	
    	sshKey.digest();
    	
    	sshKeyManager.save(sshKey);
    	return sshKey.getId();
	}
	
	@Api(order=200)
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
