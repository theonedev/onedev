package io.onedev.server.rest;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;

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
    	if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return emailAddress;
	}
	
	@Api(order=150)
	@Path("/{emailAddressId}/verified")
	@GET
	public boolean getVerified(@PathParam("emailAddressId") Long emailAddressId) {
		EmailAddress emailAddress = emailAddressManager.load(emailAddressId);
    	if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return emailAddress.isVerified();
	}
	
	@Api(order=200, description="Create new email address")
	@POST
	public Long create(@NotNull @Valid EmailAddressCreateData data) {
		User user = userManager.load(data.getOwnerId());
		if (!SecurityUtils.isAdministrator() && !user.equals(SecurityUtils.getUser()))
			throw new UnauthorizedException();

		if (emailAddressManager.findByValue(data.getValue()) != null)
			throw new ExplicitException("This email address is already used by another user");
		
		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setOwner(userManager.load(data.getOwnerId()));
		emailAddress.setValue(data.getValue());
		
		if (SecurityUtils.isAdministrator()) 
			emailAddress.setVerificationCode(null);
		
		emailAddressManager.create(emailAddress);
		return emailAddress.getId();
	}
	
	@Api(order=250, description="Set as primary email address")
	@Path("/primary")
	@POST
	public Long setAsPrimary(@NotNull Long emailAddressId) {
		EmailAddress emailAddress = emailAddressManager.load(emailAddressId);
		
		if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(SecurityUtils.getUser()))
			throw new UnauthorizedException();
		
		if (emailAddress.getOwner().isExternalManaged())
			throw new ExplicitException("Can not set primary email address for externally authenticated user");
		
		emailAddressManager.setAsPrimary(emailAddress);
		
		return emailAddressId;
	}
	
	@Api(order=260, description="Use for git operations")
	@Path("/git")
	@POST
	public Long useForGitOperations(@NotNull Long emailAddressId) {
		EmailAddress emailAddress = emailAddressManager.load(emailAddressId);
		
		if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(SecurityUtils.getUser()))
			throw new UnauthorizedException();
		
		emailAddressManager.useForGitOperations(emailAddress);
		
		return emailAddressId;
	}
	
	@Api(order=260, description="Resend verification email")
	@Path("/resend-verification-email")
	@POST
	public Long resendVerificationEmail(@NotNull Long emailAddressId) {
		EmailAddress emailAddress = emailAddressManager.load(emailAddressId);
		
		if (!SecurityUtils.isAdministrator() && !emailAddress.getOwner().equals(SecurityUtils.getUser()))
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
		EmailAddress emailAddress = emailAddressManager.load(emailAddressId);
		if (SecurityUtils.isAdministrator() 
				|| emailAddress.getOwner().equals(SecurityUtils.getUser())) {
			if (emailAddress.isPrimary() && emailAddress.getOwner().isExternalManaged()) {
				throw new ExplicitException("Can not delete primary email address of "
						+ "externally authenticated user");
			}
			if (emailAddress.getOwner().getEmailAddresses().size() == 1)
				throw new ExplicitException("At least one email address should be present for a user");
			emailAddressManager.delete(emailAddress);
			return Response.ok().build();
		} else {
			throw new UnauthorizedException();
		}
	}
	
	@EntityCreate(EmailAddress.class)
	public static class EmailAddressCreateData implements Serializable {

		private static final long serialVersionUID = 1L;

		private Long ownerId;
		
		private String value;

		@Api(order=100, description="Id of user owning this email address")
		@NotNull
		public Long getOwnerId() {
			return ownerId;
		}

		public void setOwnerId(Long ownerId) {
			this.ownerId = ownerId;
		}

		@Api(order=200, description="The email address string")
		@NotEmpty
		@Email
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
	}
	
}
