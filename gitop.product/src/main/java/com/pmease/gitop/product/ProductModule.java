package com.pmease.gitop.product;

import java.io.File;
import java.util.Properties;

import com.google.inject.name.Names;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.jetty.extensionpoints.ServerConfigurator;
import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.loader.AppName;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitop.core.setting.ServerConfig;

public class ProductModule extends AbstractPluginModule {

    @Override
	protected void configure() {
		super.configure();
		
		bindConstant().annotatedWith(AppName.class).to(Product.NAME);
		
		Properties hibernateProps = FileUtils.loadProperties(
				new File(Bootstrap.installDir, "conf/hibernate.properties")); 
		bind(Properties.class).annotatedWith(Names.named("hibernate")).toInstance(hibernateProps);
		
		Properties serverProps = FileUtils.loadProperties(
				new File(Bootstrap.installDir, "conf/server.properties")); 
		bind(Properties.class).annotatedWith(Names.named("server")).toInstance(serverProps);
		
		bind(ServerConfig.class).to(DefaultServerConfig.class);
		
		addExtension(ServerConfigurator.class, GitopServerConfigurator.class);
		addExtension(ServletContextConfigurator.class, GitopServletContextConfigurator.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return Product.class;
	}

}
