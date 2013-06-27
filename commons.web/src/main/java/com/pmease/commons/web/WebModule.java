package com.pmease.commons.web;

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;

public class WebModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(WicketServlet.class).to(DefaultWicketServlet.class);
		bind(WicketFilter.class).to(DefaultWicketFilter.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return WebPlugin.class;
	}

}
