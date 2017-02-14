package com.gitplex.commons.hibernate;

import com.google.inject.Inject;
import com.gitplex.calla.loader.AbstractPlugin;

public class HibernatePlugin extends AbstractPlugin {

	private final PersistManager persistManager;
	
	@Inject
	public HibernatePlugin(PersistManager persistManager) {
		this.persistManager = persistManager;
	}

	@Override
	public void start() {
		persistManager.start();
	}

	@Override
	public void stop() {
		persistManager.stop();
	}

}
