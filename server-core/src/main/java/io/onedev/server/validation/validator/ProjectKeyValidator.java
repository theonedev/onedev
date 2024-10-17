package io.onedev.server.validation.validator;

import io.onedev.server.annotation.ProjectKey;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class ProjectKeyValidator implements ConstraintValidator<ProjectKey, String> {

	public static final Pattern PATTERN = Pattern.compile("[A-Z][A-Z]+");
	
	private String message;

	@Override
	public void initialize(ProjectKey constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		
		if (!PATTERN.matcher(value).matches()) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0) 
				message = "Should be two or more uppercase letters";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}	
	}
	
}
