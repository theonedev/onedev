package io.onedev.server.agent;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import java.io.IOException;
import java.time.Duration;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServletFactory;

import io.onedev.agent.Agent;
import io.onedev.server.OneDev;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AgentTokenService;

@Singleton
public class ServerSocketServlet extends JettyWebSocketServlet {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private AgentTokenService tokenService;
	
	@Override
	public void configure(JettyWebSocketServletFactory factory) {
        factory.register(ServerSocket.class);		
        factory.setIdleTimeout(Duration.ofMillis(Agent.SOCKET_IDLE_TIMEOUT));
        factory.setMaxBinaryMessageSize(Agent.MAX_MESSAGE_BYTES);
		factory.setMaxTextMessageSize(Agent.MAX_MESSAGE_BYTES);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!OneDev.getInstance().isReady())
			throw new ServerNotReadyException();
		String tokenValue = SecurityUtils.getBearerToken(request);
		if (tokenValue != null && tokenService.find(tokenValue) != null)
			super.service(request, response);
		else
			response.sendError(SC_FORBIDDEN, "A valid agent token is expected");
	}
	
}