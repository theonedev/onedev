package io.onedev.server.entitymanager;

import io.onedev.server.model.Alert;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;

public interface AlertManager extends EntityManager<Alert> {
	
	void alert(String subject, @Nullable String detail, boolean mailError);

	void alert(String subject, @Nullable String detail);
	
	void clear();
	
}