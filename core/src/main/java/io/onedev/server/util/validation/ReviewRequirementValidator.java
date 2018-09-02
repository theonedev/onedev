package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.exception.OneException;
import io.onedev.server.model.Project;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.ReviewRequirement;

public class ReviewRequirementValidator implements ConstraintValidator<ReviewRequirement, String> {
	
	@Override
	public void initialize(ReviewRequirement constaintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			Project project = OneContext.get().getProject();
			try {
				io.onedev.server.util.reviewrequirement.ReviewRequirement.parse(project, value);
				return true;
			} catch (OneException e) {
				constraintContext.disableDefaultConstraintViolation();
				constraintContext.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
				return false;
			}
		}
	}
}
