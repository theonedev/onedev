package io.onedev.server;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import io.onedev.agent.Agent;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.model.AgentToken;

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
		String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (bearer != null && bearer.startsWith(Agent.BEARER + " ")) {
			String tokenValue = bearer.substring(Agent.BEARER.length() + 1);
			AgentToken token = tokenManager.find(tokenValue);
			if (token != null)
				super.service(request, response);
			else 
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid agent token");
		} else {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No agent token");
		}
	}
	
}