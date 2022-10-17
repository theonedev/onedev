package io.onedev.server;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import io.onedev.agent.Agent;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class ServerSocketServlet extends WebSocketServlet {

	private static final long serialVersionUID = 1L;
	
	private final AgentTokenManager tokenManager;

	@Inject
	public ServerSocketServlet(AgentTokenManager tokenManager) {
		this.tokenManager = tokenManager;
	}
	
	@Override
	public void configure(WebSocketServletFactory factory) {
        factory.register(ServerSocket.class);		
        factory.getPolicy().setIdleTimeout(Agent.SOCKET_IDLE_TIMEOUT);
        factory.getPolicy().setMaxBinaryMessageSize(Agent.MAX_MESSAGE_BYTES);
		factory.getPolicy().setMaxTextMessageSize(Agent.MAX_MESSAGE_BYTES);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String bearerToken = SecurityUtils.getBearerToken(request);
		if (bearerToken != null && tokenManager.find(bearerToken) != null)  
			super.service(request, response);
		else
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "A valid agent token is expected");
	}
	
}