package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.BuildQuery;

public class BuildQueryValidator implements ConstraintValidator<BuildQuery, String> {

	private String message;
	
	private boolean withCurrentUserCriteria;
	
	private boolean withUnfinishedCriteria;
	
	@Override
	public void initialize(BuildQuery constaintAnnotation) {
		message = constaintAnnotation.message();
		withCurrentUserCriteria = constaintAnnotation.withCurrentUserCriteria();
		withUnfinishedCriteria = constaintAnnotation.withUnfinishedCriteria();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			Project project = Project.get();
			try {
				io.onedev.server.search.entity.build.BuildQuery.parse(project, value, 
						withCurrentUserCriteria, withUnfinishedCriteria);
				return true;
			} catch (Exception e) {
				constraintContext.disableDefaultConstraintViolation();
				String message = this.message;
				if (message.length() == 0) {
					if (StringUtils.isNotBlank(e.getMessage()))
						message = e.getMessage();
					else
						message = "Malformed build query";
				}
				
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
		}
	}
	
}
