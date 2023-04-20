package io.onedev.server.product;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.ServerConfig;
import io.onedev.server.jetty.ServerConfigurator;
import io.onedev.server.jetty.ServletConfigurator;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.util.ProjectNameReservation;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static io.onedev.commons.bootstrap.Bootstrap.installDir;
import static io.onedev.commons.utils.FileUtils.loadProperties;
import static io.onedev.server.OneDev.getAssetsDir;
import static io.onedev.server.persistence.PersistenceUtils.loadHibernateConfig;

public class ProductModule extends AbstractPluginModule {

    @Override
	protected void configure() {
		super.configure();
		
		bind(HibernateConfig.class).toInstance(loadHibernateConfig(installDir));
		
		var file = new File(installDir, "conf/server.properties");
		ServerProperties serverProps = new ServerProperties(loadProperties(file)); 
		bind(ServerProperties.class).toInstance(serverProps);
		
		bind(ServerConfig.class).to(DefaultServerConfig.class);

		contribute(ServerConfigurator.class, ProductConfigurator.class);
		contribute(ServletConfigurator.class, ProductServletConfigurator.class);
		
		contribute(ProjectNameReservation.class, () -> {
			Set<String> reserved = new HashSet<>();
			for (var file1 : getAssetsDir().listFiles())
				reserved.add(file1.getName());
			return reserved;
		});
	}

}
