package io.onedev.server.util.validation;

import java.text.ParseException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.CronExpression;

public class CronExpressionValidator implements ConstraintValidator<CronExpression, String> {

	private String message;
	
	@Override
	public void initialize(CronExpression constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value != null) {
	        try {
	            new org.quartz.CronExpression(value);
	            return true;
	        } catch (ParseException pe) {
				constraintContext.disableDefaultConstraintViolation();
				constraintContext.buildConstraintViolationWithTemplate(message + ": " + pe.getMessage()).addConstraintViolation();
	            return false;
	        }
		} else {
			return true;
		}
	}
	
}
