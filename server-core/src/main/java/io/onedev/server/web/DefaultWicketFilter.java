package io.onedev.server.web;

import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.onedev.server.web.websocket.WebSocketFilter;

@Singleton
public class DefaultWicketFilter extends WebSocketFilter {

	private final WebApplication application;
	
	@Inject
	public DefaultWicketFilter(WebApplication application, WebSocketPolicy webSocketPolicy) {
		super(webSocketPolicy);
		
		this.application = application;
		setFilterPath("");
	}
	
	@Override
	protected IWebApplicationFactory getApplicationFactory() {
		return new IWebApplicationFactory() {

			public WebApplication createApplication(WicketFilter filter) {
				return application;
			}

			public void destroy(WicketFilter filter) {
				
			}
		};
	}

}
