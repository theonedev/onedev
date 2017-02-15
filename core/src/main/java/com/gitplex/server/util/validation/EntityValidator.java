package com.gitplex.server.util.validation;

import com.gitplex.server.persistence.AbstractEntity;

public interface EntityValidator {
	void validate(AbstractEntity entity);
}
