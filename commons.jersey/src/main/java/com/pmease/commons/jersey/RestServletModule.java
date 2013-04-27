package com.pmease.commons.jersey;

import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class RestServletModule extends JerseyServletModule {
	
	protected void configureServlets() {
		// Bind at least one resource here as otherwise Jersey will report error.
		bind(DummyResource.class);

		// Route all RESTful requests through GuiceContainer
		serve(getRestPath()).with(GuiceContainer.class);
	}
	
	protected String getRestPath() {
		return "/rest/*";
	}

}
