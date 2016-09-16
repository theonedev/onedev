package com.pmease.commons.hibernate;

import com.google.inject.Inject;
import com.pmease.commons.loader.AbstractPlugin;

public class HibernatePlugin extends AbstractPlugin {

	private final PersistManager persistService;
	
	@Inject
	public HibernatePlugin(PersistManager persistService) {
		this.persistService = persistService;
	}

	@Override
	public void start() {
		persistService.start();
	}

	@Override
	public void stop() {
		persistService.stop();
	}

}
