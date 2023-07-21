package io.onedev.server.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.AgentQuery;

public class AgentQueryValidator implements ConstraintValidator<AgentQuery, String> {

	private String message;
	
	private boolean forExecutor;
	
	@Override
	public void initialize(AgentQuery constaintAnnotation) {
		message = constaintAnnotation.message();
		forExecutor = constaintAnnotation.forExecutor();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			try {
				io.onedev.server.search.entity.agent.AgentQuery.parse(value, forExecutor);
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
