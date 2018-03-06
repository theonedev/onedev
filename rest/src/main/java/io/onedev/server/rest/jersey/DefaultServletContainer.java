package io.onedev.server.rest.jersey;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import io.onedev.server.persistence.annotation.Sessional;

@SuppressWarnings("serial")
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
