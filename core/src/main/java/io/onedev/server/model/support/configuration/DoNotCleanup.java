package io.onedev.server.model.support.configuration;

import org.hibernate.Session;

import io.onedev.server.model.Configuration;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=50, name="Do not clean up")
public class DoNotCleanup implements BuildCleanupRule {

	private static final long serialVersionUID = 1L;

	@Override
	public void cleanup(Configuration configuration, Session session) {
	}

}
