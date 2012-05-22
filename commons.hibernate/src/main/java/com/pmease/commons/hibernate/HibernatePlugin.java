package com.pmease.commons.hibernate;

import java.util.Collection;

import com.google.inject.Inject;
import com.pmease.commons.loader.AbstractPlugin;

public class HibernatePlugin extends AbstractPlugin {

	private final PersistService persistService;
	
	@Inject
	public HibernatePlugin(PersistService persistService) {
		this.persistService = persistService;
	}
		
	@Override
	public void preStartDependents() {
		persistService.start();
	}

	@Override
	public void postStopDependents() {
		persistService.stop();
	}

	@Override
	public Collection<?> getExtensions() {
		return null;
	}

}
