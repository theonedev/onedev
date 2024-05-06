package io.onedev.server.rest.resource;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.onedev.server.security.SecurityUtils.getAuthUser;

@Api(order=5010)
@Path("/email-addresses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class EmailAddressResource {

	private final UserManager userManager;
	
	private final EmailAddressManager emailAddressManager;
	
	private final SettingManager settingManager;

	@Inject
	public EmailAddressResource(UserManager userManager, EmailAddressManager emailAddressManager, 
			SettingManager settingManager) {
		this.userManager = userManager;
		this.emailAddressManager = emailAddressManager;
		this.settingManager = settingManager;
	}

	@Api(order=100)
	@Path("/{emailAddressId}")
	@GET
	public EmailAddress get(@PathParam("emailAddressId") Long emailAddressId) {
		EmailAddress emailAddress = emailAddressManager.load(emailAddressId);
    	if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return emailAddress;
	}
	
	@Api(order=150)
	@Path("/{emailAddressId}/verified")
	@GET
	public boolean getVerified(@PathParam("emailAddressId") Long emailAddressId) {
		EmailAddress emailAddress = emailAddressManager.load(emailAddressId);
    	if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return emailAddress.isVerified();
	}
	
	@Api(order=200, description="Create new email address")
	@POST
	public Long create(@NotNull @Valid EmailAddress emailAddress) {
		var owner = emailAddress.getOwner();
		if (!SecurityUtils.isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();

		if (emailAddressManager.findByValue(emailAddress.getValue()) != null)
			throw new ExplicitException("This email address is already used by another user");
		
		if (SecurityUtils.isAdministrator()) 
			emailAddress.setVerificationCode(null);
		
		emailAddressManager.create(emailAddress);
		return emailAddress.getId();
	}
	
	@Api(order=250, description="Set as primary email address")
	@Path("/primary")
	@POST
	public Long setAsPrimary(@NotNull Long emailAddressId) {
		var emailAddress = emailAddressManager.load(emailAddressId);
		var owner = emailAddress.getOwner();
		if (!SecurityUtils.isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();
		
		if (owner.isExternalManaged())
			throw new ExplicitException("Can not set primary email address for externally authenticated user");
		
		emailAddressManager.setAsPrimary(emailAddress);
		
		return emailAddressId;
	}
	
	@Api(order=260, description="Use for git operations")
	@Path("/git")
	@POST
	public Long useForGitOperations(@NotNull Long emailAddressId) {
		var emailAddress = emailAddressManager.load(emailAddressId);
		if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(getAuthUser()))
			throw new UnauthorizedException();
		
		emailAddressManager.useForGitOperations(emailAddress);
		
		return emailAddressId;
	}
	
	@Api(order=260, description="Resend verification email")
	@Path("/resend-verification-email")
	@POST
	public Long resendVerificationEmail(@NotNull Long emailAddressId) {
		var emailAddress = emailAddressManager.load(emailAddressId);
		if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(getAuthUser()))
			throw new UnauthorizedException();

		if (settingManager.getMailService() == null)
			throw new ExplicitException("Unable to send verification email as mail service is not configured");
		if (emailAddress.isVerified())
			throw new ExplicitException("Unable to send verification email as this email address is already verified");
		
		emailAddressManager.sendVerificationEmail(emailAddress);
		
		return emailAddressId;
	}
	
	@Api(order=300)
	@Path("/{emailAddressId}")
	@DELETE
	public Response delete(@PathParam("emailAddressId") Long emailAddressId) {
		var emailAddress = emailAddressManager.load(emailAddressId);
		if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(getAuthUser())) 
			throw new UnauthorizedException();
		
		if (emailAddress.isPrimary() && emailAddress.getOwner().isExternalManaged()) {
			throw new ExplicitException("Can not delete primary email address of "
					+ "externally authenticated user");
		}
		if (emailAddress.getOwner().getEmailAddresses().size() == 1)
			throw new ExplicitException("At least one email address should be present for a user");
		emailAddressManager.delete(emailAddress);
		return Response.ok().build();
	}
	
}
