package com.pmease.gitop.web.resource;

import com.google.inject.AbstractModule;

public class RestResourceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TestResource.class);
	}

}
