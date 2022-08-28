package io.onedev.server.product;

import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.DRIVER;
import static org.hibernate.cfg.AvailableSettings.PASS;
import static org.hibernate.cfg.AvailableSettings.URL;
import static org.hibernate.cfg.AvailableSettings.USER;

import java.io.File;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.persistence.HibernateProperties;
import io.onedev.server.util.ServerConfig;
import io.onedev.server.util.jetty.ServerConfigurator;
import io.onedev.server.util.jetty.ServletConfigurator;

public class ProductModule extends AbstractPluginModule {

	private static final String[] HIBERNATE_PROPS = new String[] {
			DIALECT, DRIVER, URL, USER, PASS, "hibernate.hikari.leakDetectionThreshold", 
			"hibernate.hikari.maxLifetime", "hibernate.hikari.connectionTimeout", 
			"hibernate.hikari.maximumPoolSize", "hibernate.hikari.validationTimeout",
			"hibernate.show_sql"
	};
	
    @Override
	protected void configure() {
		super.configure();
		
		File file = new File(Bootstrap.installDir, "conf/hibernate.properties"); 
		HibernateProperties hibernateProps = new HibernateProperties(FileUtils.loadProperties(file));
		String url = hibernateProps.getProperty(URL);
		hibernateProps.setProperty(URL, 
				StringUtils.replace(url, "${installDir}", Bootstrap.installDir.getAbsolutePath()));
		
		for (String prop: HIBERNATE_PROPS) {
			String env = System.getenv(prop.replace('.', '_'));
			if (env != null)
				hibernateProps.setProperty(prop, env);
		}
		
		bind(HibernateProperties.class).toInstance(hibernateProps);
		
		file = new File(Bootstrap.installDir, "conf/server.properties");
		ServerProperties serverProps = new ServerProperties(FileUtils.loadProperties(file)); 
		bind(ServerProperties.class).toInstance(serverProps);
		
		bind(ServerConfig.class).to(DefaultServerConfig.class);

		contribute(ServerConfigurator.class, ProductConfigurator.class);
		contribute(ServletConfigurator.class, ProductServletConfigurator.class);
	}

}
