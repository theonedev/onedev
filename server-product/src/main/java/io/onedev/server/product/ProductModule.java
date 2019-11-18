package io.onedev.server.product;

import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.DRIVER;
import static org.hibernate.cfg.AvailableSettings.PASS;
import static org.hibernate.cfg.AvailableSettings.URL;
import static org.hibernate.cfg.AvailableSettings.USER;

import java.io.File;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.persistence.HibernateProperties;
import io.onedev.server.util.ServerConfig;
import io.onedev.server.util.jetty.ServerConfigurator;
import io.onedev.server.util.jetty.ServletConfigurator;

public class ProductModule extends AbstractPluginModule {

    @Override
	protected void configure() {
		super.configure();
		
		File file = new File(Bootstrap.installDir, "conf/hibernate.properties"); 
		HibernateProperties hibernateProps = new HibernateProperties(FileUtils.loadProperties(file));
		String url = hibernateProps.getProperty(URL);
		hibernateProps.setProperty(URL, 
				StringUtils.replace(url, "${installDir}", Bootstrap.installDir.getAbsolutePath()));
		
		if (System.getenv(DIALECT.replace('.', '_')) != null)
			hibernateProps.setProperty(DIALECT, System.getenv(DIALECT.replace('.', '_')));
		if (System.getenv(DRIVER.replace('.', '_')) != null)
			hibernateProps.setProperty(DRIVER, System.getenv(DRIVER.replace('.', '_')));
		if (System.getenv(URL.replace('.', '_')) != null)
			hibernateProps.setProperty(URL, System.getenv(URL.replace('.', '_')));
		if (System.getenv(USER.replace('.', '_')) != null)
			hibernateProps.setProperty(USER, System.getenv(USER.replace('.', '_')));
		if (System.getenv(PASS.replace('.', '_')) != null)
			hibernateProps.setProperty(PASS, System.getenv(PASS.replace('.', '_')));
		
		String maxPoolSizeProp = "hibernate.hikari.maximumPoolSize";
		if (System.getenv(maxPoolSizeProp.replace('.', '_')) != null)
			hibernateProps.setProperty(maxPoolSizeProp, System.getenv(maxPoolSizeProp.replace('.', '_')));
		
		bind(HibernateProperties.class).toInstance(hibernateProps);
		
		file = new File(Bootstrap.installDir, "conf/server.properties");
		ServerProperties serverProps = new ServerProperties(FileUtils.loadProperties(file)); 
		bind(ServerProperties.class).toInstance(serverProps);
		
		bind(ServerConfig.class).to(DefaultServerConfig.class);

		contribute(ServerConfigurator.class, ProductConfigurator.class);
		contribute(ServletConfigurator.class, ProductServletConfigurator.class);
	}

}
