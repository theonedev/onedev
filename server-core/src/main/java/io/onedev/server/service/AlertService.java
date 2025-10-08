package io.onedev.server.service;

import io.onedev.server.model.Alert;

import org.jspecify.annotations.Nullable;

public interface AlertService extends EntityService<Alert> {
	
	void alert(String subject, @Nullable String detail, boolean mailError);

	void alert(String subject, @Nullable String detail);
	
	void clear();
	
}