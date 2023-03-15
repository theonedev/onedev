package io.onedev.server.plugin.imports.gitlab;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
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
