package io.onedev.server.product;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.ServerConfig;
import io.onedev.server.jetty.ServerConfigurator;
import io.onedev.server.jetty.ServletConfigurator;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.util.ProjectNameReservation;

import java.util.HashSet;
import java.util.Set;

import static io.onedev.commons.bootstrap.Bootstrap.installDir;
import static io.onedev.server.OneDev.getAssetsDir;

public class ProductModule extends AbstractPluginModule {

    @Override
	protected void configure() {
		super.configure();
		
		bind(HibernateConfig.class).toInstance(new HibernateConfig(installDir));
		bind(ServerConfig.class).toInstance(new ServerConfig(installDir));

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
