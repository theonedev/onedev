package com.pmease.commons.wicket;

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;

public class WicketModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(WicketServlet.class).to(CustomWicketServlet.class);
		bind(WicketFilter.class).to(CustomWicketFilter.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return WicketPlugin.class;
	}

}
