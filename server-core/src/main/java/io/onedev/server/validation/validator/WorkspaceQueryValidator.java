package io.onedev.server.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.WorkspaceQuery;
import io.onedev.server.model.Project;

public class WorkspaceQueryValidator implements ConstraintValidator<WorkspaceQuery, String> {

	private String message;

	private boolean withCurrentUserCriteria;

	@Override
	public void initialize(WorkspaceQuery constraintAnnotation) {
		message = constraintAnnotation.message();
		withCurrentUserCriteria = constraintAnnotation.withCurrentUserCriteria();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			Project project = Project.get();
			try {
				io.onedev.server.search.entity.workspace.WorkspaceQuery.parse(project, value, withCurrentUserCriteria);
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
