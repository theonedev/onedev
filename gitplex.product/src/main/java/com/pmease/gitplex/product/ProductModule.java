package com.pmease.gitplex.product;

import java.io.File;
import java.util.Properties;

import org.hibernate.cfg.Environment;

import com.google.inject.name.Names;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.HibernateProperties;
import com.pmease.commons.jetty.ServerConfigurator;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.setting.ServerConfig;

public class ProductModule extends AbstractPluginModule {

    @Override
	protected void configure() {
		super.configure();
		
		File file = new File(Bootstrap.installDir, "conf/hibernate.properties"); 
		HibernateProperties hibernateProps = new HibernateProperties(FileUtils.loadProperties(file));
		String url = hibernateProps.getProperty(Environment.URL);
		hibernateProps.setProperty(Environment.URL, 
				StringUtils.replace(url, "${installDir}", Bootstrap.installDir.getAbsolutePath()));
		
		bind(HibernateProperties.class).toInstance(hibernateProps);
		
		Properties serverProps = FileUtils.loadProperties(
				new File(Bootstrap.installDir, "conf/server.properties")); 
		bind(Properties.class).annotatedWith(Names.named("server")).toInstance(serverProps);
		
		bind(ServerConfig.class).to(DefaultServerConfig.class);

		contribute(ServerConfigurator.class, ProductConfigurator.class);
		contribute(ServletConfigurator.class, ProductServletConfigurator.class);
		
	}

}
