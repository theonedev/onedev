package io.onedev.server.product;

import java.io.File;

import org.hibernate.cfg.Environment;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.persistence.HibernateProperties;
import io.onedev.server.util.jetty.ServerConfigurator;
import io.onedev.server.util.jetty.ServletConfigurator;
import io.onedev.server.util.serverconfig.ServerConfig;

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
