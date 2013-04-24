package com.pmease.commons.product;

import java.io.File;
import java.util.Properties;

import org.apache.wicket.protocol.http.WebApplication;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.Hibernate;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.util.FileUtils;

public class PluginModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		Properties hibernateProps = FileUtils.loadProperties(
				new File(Bootstrap.installDir, "conf/hibernate.properties")); 
		bind(Properties.class).annotatedWith(Hibernate.class).toInstance(hibernateProps);

		bind(HelloGuice.class);
		bind(GuicyInterface.class).to(GuicyInterfaceImpl.class);
		
		bind(WebApplication.class).to(WicketConfig.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return Plugin.class;
	}

}
