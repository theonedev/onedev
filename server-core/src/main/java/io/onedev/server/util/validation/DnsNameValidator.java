package io.onedev.server.util.validation;

import java.util.function.Function;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.util.validation.annotation.DnsName;

public class DnsNameValidator implements ConstraintValidator<DnsName, String> {

	private static final Pattern PATTERN = Pattern.compile("[a-z0-9]([-a-z0-9]*[a-z0-9])?");
	
	private boolean interpolative;
	
	private String message;
	
	@Override
	public void initialize(DnsName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;
		
		if (interpolative && !Interpolated.get()) try {
			value = Interpolative.fromString(value).interpolateWith(new Function<String, String>() {

				@Override
				public String apply(String t) {
					return "a";
				}
				
			});
		} catch (Exception e) {
			return true; // will be handled by interpolative validator
		}
		
		if (!PATTERN.matcher(value).matches()) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
