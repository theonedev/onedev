package com.pmease.commons.wicket;

import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pmease.commons.wicket.websocket.WebSocketFilter;

@Singleton
public class DefaultWicketFilter extends WebSocketFilter {

	private final AbstractWicketConfig wicketConfig;
	
	@Inject
	public DefaultWicketFilter(AbstractWicketConfig wicketConfig) {
		this.wicketConfig = wicketConfig;
		setFilterPath("");
	}
	
	@Override
	protected IWebApplicationFactory getApplicationFactory() {
		return new IWebApplicationFactory() {

			public WebApplication createApplication(WicketFilter filter) {
				return wicketConfig;
			}

			public void destroy(WicketFilter filter) {
				
			}
		};
	}

}
