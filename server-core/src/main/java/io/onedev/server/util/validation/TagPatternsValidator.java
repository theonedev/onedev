package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.TagPatterns;

public class TagPatternsValidator implements ConstraintValidator<TagPatterns, String> {
	
	@Override
	public void initialize(TagPatterns constaintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			try {
				PatternSet.fromString(value);
				return true;
			} catch (Exception e) {
				constraintContext.disableDefaultConstraintViolation();
				if (StringUtils.isNotBlank(e.getMessage()))
					constraintContext.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
				else
					constraintContext.buildConstraintViolationWithTemplate("Malformed tag patterns").addConstraintViolation();
				return false;
			}
		}
	}
}
