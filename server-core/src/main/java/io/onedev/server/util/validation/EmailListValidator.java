package io.onedev.server.util.validation;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;

import io.onedev.server.util.validation.annotation.EmailList;

public class EmailListValidator implements ConstraintValidator<EmailList, List<String>> {

	private String message;
	
	@Override
	public void initialize(EmailList constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(List<String> value, ConstraintValidatorContext constraintContext) {
		if (value == null || value.isEmpty()) 
			return true;

		boolean isValid = true;
		EmailValidator validator = new EmailValidator();
		for (String email: value) {
			if (!validator.isValid(email, constraintContext)) {
				String message = this.message;
				if (message.length() == 0)
					message = "Invalid email address: " + email;
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				isValid = false;
			}
		}
		if (!isValid)
			constraintContext.disableDefaultConstraintViolation();
		return isValid;
	}
	
}
