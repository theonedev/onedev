package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.reviewrequirement.InvalidReviewRuleException;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.web.editable.annotation.ReviewRequirementSpec;

public class ReviewRequirementSpecValidator implements ConstraintValidator<ReviewRequirementSpec, String> {
	
	public void initialize(ReviewRequirementSpec constaintAnnotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			try {
				new ReviewRequirement(value);
				return true;
			} catch (InvalidReviewRuleException e) {
				constraintContext.disableDefaultConstraintViolation();
				constraintContext.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
				return false;
			}
		}
	}
}
