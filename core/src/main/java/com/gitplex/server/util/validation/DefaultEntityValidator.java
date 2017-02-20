package com.gitplex.server.util.validation;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.gitplex.server.model.AbstractEntity;
import com.gitplex.server.model.Account;

@Singleton
public class DefaultEntityValidator implements EntityValidator {

	protected final Validator validator;
	
	@Inject
	public DefaultEntityValidator(Validator validator) {
		this.validator = validator;
	}
	
	protected void reportError(AbstractEntity entity, ConstraintViolation<?> violation) {
		String errorInfo = String.format("Error validating entity {entity class: %s, entity id: %d, entity property: %s, error message: %s}", 
				entity.getClass(), entity.getId(), violation.getPropertyPath().toString(), violation.getMessage());
		throw new RuntimeException(errorInfo);
	}
	
	@Override
	public void validate(AbstractEntity entity) {
		if (entity instanceof Account) {
			Account account = (Account) entity;
			Set<String> excludeProperties;
			if (account.isOrganization()) {
				excludeProperties = Account.getOrganizationExcludeProperties();
			} else {
				excludeProperties = Account.getUserExcludeProperties();
			}
			for (ConstraintViolation<?> violation: validator.validate(account)) {
				if (!excludeProperties.contains(violation.getPropertyPath().toString())) {
					reportError(account, violation);
				}
			}
		} else {
			for (ConstraintViolation<?> violation: validator.validate(entity)) {
				reportError(entity, violation);
			}
		}
	}

}
