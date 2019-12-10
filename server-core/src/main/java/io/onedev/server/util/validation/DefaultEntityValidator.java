package io.onedev.server.util.validation;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import io.onedev.server.model.AbstractEntity;

@Singleton
public class DefaultEntityValidator implements EntityValidator {

	protected final Validator validator;
	
	@Inject
	public DefaultEntityValidator(Validator validator) {
		this.validator = validator;
	}
	
	protected void reportError(AbstractEntity entity, ConstraintViolation<?> violation) {
		String errorInfo = String.format("Error validating entity (entity class: %s, entity id: %d, entity property: %s, error message: %s)", 
				entity.getClass(), entity.getId(), violation.getPropertyPath().toString(), violation.getMessage());
		throw new RuntimeException(errorInfo);
	}
	
	@Override
	public void validate(AbstractEntity entity) {
		if (entity.getId() > 0) {
			for (ConstraintViolation<?> violation: validator.validate(entity)) 
				reportError(entity, violation);
		}
	}

}
