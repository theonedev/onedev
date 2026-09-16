package io.onedev.server.rest;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import io.onedev.server.persistence.annotation.Sessional;

@Singleton
public class DefaultServletContainer extends ServletContainer {
	
	@Inject
	public DefaultServletContainer(ResourceConfig resourceConfig) {
		super(resourceConfig);
	}

	@Sessional
	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		super.service(req, res);
	}
	
}
