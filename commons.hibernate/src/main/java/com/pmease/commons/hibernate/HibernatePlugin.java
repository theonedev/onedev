package com.pmease.commons.hibernate;

import com.google.inject.Inject;
import com.pmease.commons.loader.AbstractPlugin;

public class HibernatePlugin extends AbstractPlugin {

	private final PersistService persistService;
	
	private final IdManager idManager;
	
	@Inject
	public HibernatePlugin(PersistService persistService, IdManager idManager) {
		this.persistService = persistService;
		this.idManager = idManager;
	}

	@Override
	public void start() {
		persistService.start();
		idManager.init();
	}

	@Override
	public void stop() {
		persistService.stop();
	}

}
