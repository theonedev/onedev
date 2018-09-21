package io.onedev.server.model.support.configuration;

import java.io.Serializable;

import org.hibernate.Session;

import io.onedev.server.model.Configuration;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface BuildCleanupStrategy extends Serializable {

	void cleanup(Configuration configuration, Session session);
	
}
