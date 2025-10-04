package io.onedev.server.rest.resource;

import static io.onedev.server.security.SecurityUtils.getAuthUser;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
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

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/email-addresses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class EmailAddressResource {
	
	private final EmailAddressService emailAddressService;
	
	private final SettingService settingService;

	private final AuditService auditService;

	@Inject
	public EmailAddressResource(EmailAddressService emailAddressService, SettingService settingService, AuditService auditService) {
		this.emailAddressService = emailAddressService;
		this.settingService = settingService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{emailAddressId}")
	@GET
	public EmailAddress getEmailAddress(@PathParam("emailAddressId") Long emailAddressId) {
		EmailAddress emailAddress = emailAddressService.load(emailAddressId);
    	if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return emailAddress;
	}
	
	@Api(order=150)
	@Path("/{emailAddressId}/verified")
	@GET
	public boolean isEmailAddressVerified(@PathParam("emailAddressId") Long emailAddressId) {
		EmailAddress emailAddress = emailAddressService.load(emailAddressId);
    	if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return emailAddress.isVerified();
	}
	
	@Api(order=200, description="Create new email address")
	@POST
	public Long createEmailAddress(@NotNull @Valid EmailAddress emailAddress) {
		var owner = emailAddress.getOwner();
		if (!SecurityUtils.isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();
		else if (owner.isDisabled())
			throw new ExplicitException("Can not set email address for disabled user");
		else if (owner.isServiceAccount())
			throw new ExplicitException("Can not set email address for service account");
		else if (emailAddressService.findByValue(emailAddress.getValue()) != null)
			throw new ExplicitException("This email address is already used by another user");
		
		if (SecurityUtils.isAdministrator()) 
			emailAddress.setVerificationCode(null);
		
		emailAddressService.create(emailAddress);

		if (!getAuthUser().equals(owner)) 
			auditService.audit(null, "added email address \"" + emailAddress.getValue() + "\" in account \"" + owner.getName() + "\" via RESTful API", null, null);
		return emailAddress.getId();
	}
	
	@Api(order=220, description="Set as public email address")
	@Path("/public")
	@POST
	public Long setAsPublic(@NotNull Long emailAddressId) {
		var emailAddress = emailAddressService.load(emailAddressId);
		var owner = emailAddress.getOwner();
		if (!SecurityUtils.isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();
				
		emailAddressService.setAsPublic(emailAddress);

		if (!getAuthUser().equals(owner)) 
			auditService.audit(null, "set email address \"" + emailAddress.getValue() + "\" as public in account \"" + owner.getName() + "\" via RESTful API", null, null);
		
		return emailAddressId;
	}

	@Api(order=230, description="Set as private email address")
	@Path("/private")
	@POST
	public Long setAsPrivate(@NotNull Long emailAddressId) {
		var emailAddress = emailAddressService.load(emailAddressId);
		var owner = emailAddress.getOwner();
		if (!SecurityUtils.isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();
		
		emailAddressService.setAsPrivate(emailAddress);

		if (!getAuthUser().equals(owner)) 
			auditService.audit(null, "set email address \"" + emailAddress.getValue() + "\" as private in account \"" + owner.getName() + "\" via RESTful API", null, null);
		
		return emailAddressId;
	}

	@Api(order=250, description="Set as primary email address")
	@Path("/primary")
	@POST
	public Long setAsPrimary(@NotNull Long emailAddressId) {
		var emailAddress = emailAddressService.load(emailAddressId);
		var owner = emailAddress.getOwner();
		if (!SecurityUtils.isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();
		
		if (owner.getPassword() == null)
			throw new ExplicitException("Can not set primary email address for externally authenticated user");
		
		emailAddressService.setAsPrimary(emailAddress);

		if (!getAuthUser().equals(owner)) 
			auditService.audit(null, "set email address \"" + emailAddress.getValue() + "\" as primary in account \"" + owner.getName() + "\" via RESTful API", null, null);
		
		return emailAddressId;
	}
	
	@Api(order=260, description="Use for git operations")
	@Path("/git")
	@POST
	public Long useForGitOperations(@NotNull Long emailAddressId) {
		var emailAddress = emailAddressService.load(emailAddressId);
		if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(getAuthUser()))
			throw new UnauthorizedException();
		
		emailAddressService.useForGitOperations(emailAddress);
		
		if (!getAuthUser().equals(emailAddress.getOwner())) 
			auditService.audit(null, "specified email address \"" + emailAddress.getValue() + "\" for git operations in account \"" + emailAddress.getOwner().getName() + "\" via RESTful API", null, null);
		
		return emailAddressId;
	}
	
	@Api(order=260, description="Resend verification email")
	@Path("/resend-verification-email")
	@POST
	public Long resendVerificationEmail(@NotNull Long emailAddressId) {
		var emailAddress = emailAddressService.load(emailAddressId);
		if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(getAuthUser()))
			throw new UnauthorizedException();

		if (settingService.getMailConnector() == null)
			throw new ExplicitException("Unable to send verification email as mail service is not configured");
		if (emailAddress.isVerified())
			throw new ExplicitException("Unable to send verification email as this email address is already verified");
		
		emailAddressService.sendVerificationEmail(emailAddress);
		
		return emailAddressId;
	}
	
	@Api(order=300)
	@Path("/{emailAddressId}")
	@DELETE
	public Response deleteEmailAddress(@PathParam("emailAddressId") Long emailAddressId) {
		var emailAddress = emailAddressService.load(emailAddressId);
		if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(getAuthUser())) 
			throw new UnauthorizedException();
		
		if (emailAddress.isPrimary() && emailAddress.getOwner().getPassword() == null) {
			throw new ExplicitException("Can not delete primary email address of "
					+ "externally authenticated user");
		}
		if (emailAddress.getOwner().getEmailAddresses().size() == 1)
			throw new ExplicitException("At least one email address should be present for a user");
		emailAddressService.delete(emailAddress);

		if (!getAuthUser().equals(emailAddress.getOwner())) 
			auditService.audit(null, "deleted email address \"" + emailAddress.getValue() + "\" from account \"" + emailAddress.getOwner().getName() + "\" via RESTful API", null, null);

		return Response.ok().build();
	}
	
}
