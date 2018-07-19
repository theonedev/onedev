package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.model.Project;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.PullRequestQuery;
import io.onedev.utils.StringUtils;

public class PullRequestQueryValidator implements ConstraintValidator<PullRequestQuery, String> {

	@Override
	public void initialize(PullRequestQuery constaintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			Project project = OneContext.get().getProject();
			try {
				io.onedev.server.model.support.pullrequest.query.PullRequestQuery.parse(project, value, true);
				return true;
			} catch (Exception e) {
				constraintContext.disableDefaultConstraintViolation();
				String message;
				if (StringUtils.isNotBlank(e.getMessage()))
					message = e.getMessage();
				else
					message = "Malformed pull request query";
				
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
		}
	}
	
}
