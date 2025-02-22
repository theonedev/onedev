package io.onedev.server.validation.validator;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.PackQuery;
import io.onedev.server.model.Project;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PackQueryValidator implements ConstraintValidator<PackQuery, String> {

	private String message;
	
	private boolean withCurrentUserCriteria;
	
	@Override
	public void initialize(PackQuery constaintAnnotation) {
		message = constaintAnnotation.message();
		withCurrentUserCriteria = constaintAnnotation.withCurrentUserCriteria();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			Project project = Project.get();
			try {
				io.onedev.server.search.entity.pack.PackQuery.parse(project, value, withCurrentUserCriteria);
				return true;
			} catch (Exception e) {
				constraintContext.disableDefaultConstraintViolation();
				String message = this.message;
				if (message.length() == 0) {
					if (StringUtils.isNotBlank(e.getMessage()))
						message = e.getMessage();
					else
						message = "Malformed query";
				}
				
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
		}
	}
	
}
