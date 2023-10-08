package io.onedev.server.web.page.admin.usermanagement;

import java.io.Serializable;
import java.util.List;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;

import com.google.common.base.Splitter;

import io.onedev.server.OneDev;
import io.onedev.server.manager.EmailAddressManager;
import io.onedev.server.manager.UserInvitationManager;
import io.onedev.server.validation.Validatable;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;

@Editable
@ClassValidating
public class NewInvitationBean implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private String emailAddresses;

	@Editable(description="Specify email addresses to send invitations, with one per line")
	@Multiline
	@NotEmpty
	public String getEmailAddresses() {
		return emailAddresses;
	}

	public void setEmailAddresses(String emailAddresses) {
		this.emailAddresses = emailAddresses;
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
