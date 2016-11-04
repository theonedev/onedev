package com.gitplex.server.core;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.gitplex.commons.hibernate.AbstractEntity;
import com.gitplex.commons.hibernate.DefaultEntityValidator;
import com.gitplex.server.core.entity.Account;

@Singleton
public class CoreEntityValidator extends DefaultEntityValidator {

	@Inject
	public CoreEntityValidator(Validator validator) {
		super(validator);
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
			super.validate(entity);
		}
	}

}
