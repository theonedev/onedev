package io.onedev.server.ee.sendgrid;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class SendgridServlet extends HttpServlet {
	
	private final MessageManager messageManager;
	
	@Inject
	public SendgridServlet(MessageManager messageManager) {
		this.messageManager = messageManager;
	}	
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		messageManager.process(req, resp);
	}
	
}
