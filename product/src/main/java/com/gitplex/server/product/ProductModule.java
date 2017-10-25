package com.gitplex.server.product;

import java.io.File;

import org.hibernate.cfg.Environment;

import com.gitplex.launcher.loader.AbstractPluginModule;
import com.gitplex.launcher.bootstrap.Bootstrap;
import com.gitplex.server.persistence.HibernateProperties;
import com.gitplex.utils.FileUtils;
import com.gitplex.server.util.jetty.ServerConfigurator;
import com.gitplex.server.util.jetty.ServletConfigurator;
import com.gitplex.server.util.serverconfig.ServerConfig;
import com.gitplex.utils.StringUtils;

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
		
		file = new File(Bootstrap.installDir, "conf/server.properties");
		ServerProperties serverProps = new ServerProperties(FileUtils.loadProperties(file)); 
		bind(ServerProperties.class).toInstance(serverProps);
		
		bind(ServerConfig.class).to(DefaultServerConfig.class);

		contribute(ServerConfigurator.class, ProductConfigurator.class);
		contribute(ServletConfigurator.class, ProductServletConfigurator.class);
		
	}

}
