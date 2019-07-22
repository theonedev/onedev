package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

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
			try {
				io.onedev.server.util.reviewrequirement.ReviewRequirement.fromString(value);
				return true;
			} catch (Exception e) {
				constraintContext.disableDefaultConstraintViolation();
				if (StringUtils.isNotBlank(e.getMessage()))
					constraintContext.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
				else
					constraintContext.buildConstraintViolationWithTemplate("Malformed review requirement").addConstraintViolation();
				return false;
			}
		}
	}
}
