package com.pmease.commons.wicket;

import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class WebFilter extends WicketFilter {

	private final WebApplication webApplication;
	
	@Inject
	public WebFilter(WebApplication webApplication) {
		this.webApplication = webApplication;
		setFilterPath("");
	}
	
	@Override
	protected IWebApplicationFactory getApplicationFactory() {
		return new IWebApplicationFactory() {

			public WebApplication createApplication(WicketFilter filter) {
				return webApplication;
			}

			public void destroy(WicketFilter filter) {
				
			}
		};
	}

}
