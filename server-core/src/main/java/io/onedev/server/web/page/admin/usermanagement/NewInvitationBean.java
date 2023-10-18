package io.onedev.server.web.page.admin.usermanagement;

import java.io.Serializable;
import java.util.List;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;

import com.google.common.base.Splitter;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.UserInvitationManager;
import io.onedev.server.validation.Validatable;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;

@Editable
@ClassValidating
public class NewInvitationBean implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private String emailAddresses;
	
	private boolean inviteAsGuest;

	@Editable(order=100, description="Specify email addresses to send invitations, with one per line")
	@Multiline
	@NotEmpty
	public String getEmailAddresses() {
		return emailAddresses;
	}

	public void setEmailAddresses(String emailAddresses) {
		this.emailAddresses = emailAddresses;
	}

	@Editable(order=200, description = "Whether or not to invite the user as <a href='https://docs.onedev.io/concepts#guest-user' target='_blank'>guest</a>")
	public boolean isInviteAsGuest() {
		return inviteAsGuest;
	}

	public void setInviteAsGuest(boolean inviteAsGuest) {
		this.inviteAsGuest = inviteAsGuest;
	}

	public List<String> getListOfEmailAddresses() {
		return Splitter.on("\n").omitEmptyStrings().trimResults().splitToList(getEmailAddresses());
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean found = false;
		for (String emailAddress: getListOfEmailAddresses()) {
			if (!new EmailValidator().isValid(emailAddress, null)) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Invalid email address: " + emailAddress)
						.addPropertyNode("emailAddresses").addConstraintViolation();
				return false;
			} else if (OneDev.getInstance(EmailAddressManager.class).findByValue(emailAddress) != null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Email address already in use: " + emailAddress)
						.addPropertyNode("emailAddresses").addConstraintViolation();
				return false;
			} else if (OneDev.getInstance(UserInvitationManager.class).findByEmailAddress(emailAddress) != null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Email address already invited: " + emailAddress)
						.addPropertyNode("emailAddresses").addConstraintViolation();
				return false;
			} else {
				found = true;
			}
		}
		if (!found) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("At least one email address should be specified")
					.addPropertyNode("emailAddresses").addConstraintViolation();
			return false;
		}
		return true;
	}
	
}
