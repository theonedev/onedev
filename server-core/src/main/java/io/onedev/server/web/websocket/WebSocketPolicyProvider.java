package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.eclipse.jetty.websocket.api.WebSocketPolicy;

import io.onedev.server.util.ServerConfig;

@Singleton
public class WebSocketPolicyProvider implements Provider<WebSocketPolicy> {

	private final ServerConfig serverConfig;
	
	@Inject
	public WebSocketPolicyProvider(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
	
	@Override
	public WebSocketPolicy get() {
		WebSocketPolicy policy = WebSocketPolicy.newServerPolicy();
		policy.setIdleTimeout(serverConfig.getSessionTimeout()*1000);
		return policy;
	}

}
