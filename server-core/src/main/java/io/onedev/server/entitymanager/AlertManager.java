package io.onedev.server.entitymanager;

import io.onedev.server.model.Alert;
import io.onedev.server.persistence.dao.EntityManager;

public interface AlertManager extends EntityManager<Alert> {
	
	void alert(String message);
	
}