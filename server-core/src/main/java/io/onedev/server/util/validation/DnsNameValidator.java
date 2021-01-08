package io.onedev.server.util.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.DnsName;

public class DnsNameValidator implements ConstraintValidator<DnsName, String> {

	private static final Pattern PATTERN = Pattern.compile("[a-zA-Z0-9]([-a-zA-Z0-9]*[a-zA-Z0-9])?");
	
	private String message;
	
	@Override
	public void initialize(DnsName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;
		
		if (!PATTERN.matcher(value).matches()) {
			String message = this.message;
			if (message.length() == 0) {
				message = "Should only contain alphanumberic characters or '-', and can only "
						+ "start and end with alphanumeric characters";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
