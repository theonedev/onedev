package io.onedev.server.web.page.admin.usermanagement;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;

import com.google.common.base.Splitter;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.UserInvitationService;
import io.onedev.server.validation.Validatable;

@Editable
@ClassValidating
public class NewInvitationBean implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private String emailAddresses;
	
	@Editable(order=100, description="Specify email addresses to send invitations, with one per line")
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
				context.buildConstraintViolationWithTemplate(MessageFormat.format(_T("Invalid email address: {0}"), emailAddress))
						.addPropertyNode("emailAddresses").addConstraintViolation();
				return false;
			} else if (OneDev.getInstance(EmailAddressService.class).findByValue(emailAddress) != null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(MessageFormat.format(_T("Email address already in use: {0}"), emailAddress))
						.addPropertyNode("emailAddresses").addConstraintViolation();
				return false;
			} else if (OneDev.getInstance(UserInvitationService.class).findByEmailAddress(emailAddress) != null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(MessageFormat.format(_T("Email address already invited: {0}"), emailAddress))
						.addPropertyNode("emailAddresses").addConstraintViolation();
				return false;
			} else {
				found = true;
			}
		}
		if (!found) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(_T("At least one email address should be specified"))
					.addPropertyNode("emailAddresses").addConstraintViolation();
			return false;
		}
		return true;
	}
	
}
