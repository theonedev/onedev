package com.turbodev.server.util.validation;

import com.turbodev.server.model.AbstractEntity;

public interface EntityValidator {
	void validate(AbstractEntity entity);
}
