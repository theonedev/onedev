package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneException;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.BuildQuery;

public class BuildQueryValidator implements ConstraintValidator<BuildQuery, String> {

	private String message;
	
	@Override
	public void initialize(BuildQuery constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			Project project = Project.get();
			try {
				io.onedev.server.search.entity.build.BuildQuery buildQuery = 
						io.onedev.server.search.entity.build.BuildQuery.parse(project, value);
				if (buildQuery.needsLogin()) { 
					String message = this.message;
					if (message.length() == 0)
						message = "This query needs login which is not supported here";
					throw new OneException(message);
				}
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
