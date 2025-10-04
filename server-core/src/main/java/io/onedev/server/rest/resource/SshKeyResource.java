package io.onedev.server.rest.resource;

import static io.onedev.server.security.SecurityUtils.getAuthUser;

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

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.SshKeyService;
import io.onedev.server.model.SshKey;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/ssh-keys")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class SshKeyResource {

	private final SshKeyService sshKeyService;

	private final AuditService auditService;

	@Inject
	public SshKeyResource(SshKeyService sshKeyService, AuditService auditService) {
		this.sshKeyService = sshKeyService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{sshKeyId}")
	@GET
	public SshKey getKey(@PathParam("sshKeyId") Long sshKeyId) {
		SshKey sshKey = sshKeyService.load(sshKeyId);
    	if (!SecurityUtils.isAdministrator() && !sshKey.getOwner().equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return sshKey;
	}
	
	@Api(order=150, description="Create new ssh key")
	@POST
	public Long createKey(SshKey sshKey) {
    	if (!SecurityUtils.isAdministrator() && !sshKey.getOwner().equals(getAuthUser())) 
			throw new UnauthorizedException();
		
		sshKey.setCreatedAt(new Date());
    	sshKey.generateFingerprint();
    	
    	sshKeyService.create(sshKey);
		if (!getAuthUser().equals(sshKey.getOwner())) {
			var newAuditContent = VersionedXmlDoc.fromBean(sshKey).toXML();
			auditService.audit(null, "created ssh key in account \"" + sshKey.getOwner().getName() + "\" via RESTful API", null, newAuditContent);
		}
    	return sshKey.getId();
	}
	
	@Api(order=200)
	@Path("/{sshKeyId}")
	@DELETE
	public Response deleteKey(@PathParam("sshKeyId") Long sshKeyId) {
		SshKey sshKey = sshKeyService.load(sshKeyId);
    	if (!SecurityUtils.isAdministrator() && !sshKey.getOwner().equals(getAuthUser())) 
			throw new UnauthorizedException();
		sshKeyService.delete(sshKey);
		if (!getAuthUser().equals(sshKey.getOwner())) {
			var oldAuditContent = VersionedXmlDoc.fromBean(sshKey).toXML();
			auditService.audit(null, "deleted ssh key from account \"" + sshKey.getOwner().getName() + "\" via RESTful API", oldAuditContent, null);
		}
		return Response.ok().build();
	}
	
}
