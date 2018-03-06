package io.onedev.server.util.validation;

import io.onedev.server.model.AbstractEntity;

public interface EntityValidator {
	void validate(AbstractEntity entity);
}
