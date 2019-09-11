package io.onedev.server.util.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.DnsName;

public class DnsNameValidator implements ConstraintValidator<DnsName, String> {

	private static final Pattern PATTERN = Pattern.compile("[a-z0-9]([-a-z0-9]*[a-z0-9])?");
	
	@Override
	public void initialize(DnsName constaintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value != null && !PATTERN.matcher(value).matches()) {
			String errorMessage = "can only contain alphanumberic characters or '-', and can only "
					+ "start and end with alphanumeric characters";
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
