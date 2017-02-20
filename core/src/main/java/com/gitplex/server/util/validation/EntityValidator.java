package com.gitplex.server.util.validation;

import com.gitplex.server.model.AbstractEntity;

public interface EntityValidator {
	void validate(AbstractEntity entity);
}
