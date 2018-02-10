package com.turbodev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.turbodev.server.util.editable.annotation.ReviewRequirementSpec;
import com.turbodev.server.util.reviewrequirement.InvalidReviewRuleException;
import com.turbodev.server.util.reviewrequirement.ReviewRequirement;

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
