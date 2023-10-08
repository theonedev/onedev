package io.onedev.server.ee.sendgrid;

import io.onedev.server.jetty.ServletConfigurator;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SendgridServletConfigurator implements ServletConfigurator {
	
	private final SendgridServlet sendgridServlet;
	
	@Inject
	public SendgridServletConfigurator(SendgridServlet sendgridServlet) {
		this.sendgridServlet = sendgridServlet;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		context.addServlet(new ServletHolder(sendgridServlet), "/~sendgrid/*");
	}
	
}
