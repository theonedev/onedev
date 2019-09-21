package io.onedev.server.util.validation;

import java.util.function.Function;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.util.validation.annotation.ProjectName;

public class ProjectNameValidator implements ConstraintValidator<ProjectName, String> {

	private static final Pattern PATTERN = Pattern.compile("[\\w-\\.]+");
	
	private boolean interpolative;
	
	private String message;
	
	@Override
	public void initialize(ProjectName constaintAnnotation) {
		message = constaintAnnotation.message();
		interpolative = constaintAnnotation.interpolative();
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
			String message = this.message;
			if (message.length() == 0)
				message = "Only alphanumeric, underscore, dash, and dot are accepted";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (value.equals("new")) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0)
				message = "'" + value + "' is a reserved name";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}	
	}
	
}
