package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.CommitQuery;

public class CommitQueryValidator implements ConstraintValidator<CommitQuery, String> {

	@Override
	public void initialize(CommitQuery constaintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			try {
				io.onedev.server.search.commit.CommitQuery.parse(OneContext.get().getProject(), value);
				return true;
			} catch (Exception e) {
				constraintContext.disableDefaultConstraintViolation();
				String message;
				if (StringUtils.isNotBlank(e.getMessage()))
					message = e.getMessage();
				else
					message = "Malformed commit query";
				
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
		}
	}

}
