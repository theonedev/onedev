package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneException;
import io.onedev.server.model.Project;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.BuildQuery;

public class BuildQueryValidator implements ConstraintValidator<BuildQuery, String> {

	private boolean noLoginSupport;
	
	@Override
	public void initialize(BuildQuery constaintAnnotation) {
		noLoginSupport = constaintAnnotation.noLoginSupport();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			Project project = OneContext.get().getProject();
			try {
				io.onedev.server.search.entity.build.BuildQuery buildQuery = 
						io.onedev.server.search.entity.build.BuildQuery.parse(project, value, true);
				if (noLoginSupport && buildQuery.needsLogin()) 
					throw new OneException("This query needs login which is not supported here");
				return true;
			} catch (Exception e) {
				constraintContext.disableDefaultConstraintViolation();
				String message;
				if (StringUtils.isNotBlank(e.getMessage()))
					message = e.getMessage();
				else
					message = "Malformed build query";
				
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
		}
	}
	
}
