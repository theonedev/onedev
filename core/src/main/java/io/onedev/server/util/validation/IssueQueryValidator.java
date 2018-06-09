package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.model.Project;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.utils.StringUtils;

public class IssueQueryValidator implements ConstraintValidator<IssueQuery, String> {

	private boolean allowSort;
	
	@Override
	public void initialize(IssueQuery constaintAnnotation) {
		allowSort = constaintAnnotation.allowSort();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			Project project = OneContext.get().getProject();
			try {
				io.onedev.server.model.support.issue.query.IssueQuery.parse(project, value, allowSort, true);
				return true;
			} catch (Exception e) {
				constraintContext.disableDefaultConstraintViolation();
				String message;
				if (StringUtils.isNotBlank(e.getMessage()))
					message = e.getMessage();
				else
					message = "Malformed issue query";
				
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
		}
	}
	
}
