package com.pmease.commons.validation;

import javax.validation.Configuration;

public interface ValidationConfigurator {
	void configure(Configuration<?> configuration);
}
