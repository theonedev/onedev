package io.onedev.server.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.JobMatch;

public class JobMatchValidator implements ConstraintValidator<JobMatch, String> {

	private String message;
	
	private boolean withProjectCriteria;

	private boolean withJobCriteria;
	
	@Override
	public void initialize(JobMatch constaintAnnotation) {
		message = constaintAnnotation.message();	
		withProjectCriteria = constaintAnnotation.withProjectCriteria();
		withJobCriteria = constaintAnnotation.withJobCriteria();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			try {
				io.onedev.server.job.match.JobMatch.parse(value, withProjectCriteria, withJobCriteria);
				return true;
			} catch (Exception e) {
				constraintContext.disableDefaultConstraintViolation();
				String message = this.message;
				if (message.length() == 0) {
					if (StringUtils.isNotBlank(e.getMessage()))
						message = e.getMessage();
					else
						message = "Malformed job match";
				}
				
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
		}
	}
	
}
