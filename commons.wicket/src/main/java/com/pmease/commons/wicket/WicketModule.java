package com.pmease.commons.wicket;

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;

import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.wicket.editable.EditSupport;

public class WicketModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(WicketServlet.class).to(DefaultWicketServlet.class);
		bind(WicketFilter.class).to(DefaultWicketFilter.class);

		contribute(ServletConfigurator.class, WicketServletConfigurator.class);
		
		contributeFromPackage(EditSupport.class, EditSupport.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return WicketPlugin.class;
	}

}
