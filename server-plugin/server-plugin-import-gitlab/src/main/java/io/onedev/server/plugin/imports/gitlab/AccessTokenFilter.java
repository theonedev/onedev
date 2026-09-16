package io.onedev.server.plugin.imports.gitlab;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

@Priority(Priorities.AUTHENTICATION)
class AccessTokenFilter implements ClientRequestFilter {

    private final String accessToken;

    public AccessTokenFilter(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void filter(ClientRequestContext request) throws IOException {
		request.getHeaders().add("Private-Token", accessToken);
    }
	
}
