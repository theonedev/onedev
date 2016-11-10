package ${package};

import java.io.File;
import java.util.Properties;

import org.apache.wicket.protocol.http.WebApplication;

import com.gitplex.commons.bootstrap.Bootstrap;
import com.gitplex.commons.loader.AbstractPlugin;
import com.gitplex.commons.loader.AbstractPluginModule;
import com.gitplex.commons.util.FileUtils;
import com.gitplex.commons.hibernate.Hibernate;

public class PluginModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		Properties hibernateProps = FileUtils.loadProperties(
				new File(Bootstrap.installDir, "conf/hibernate.properties")); 
		bind(Properties.class).annotatedWith(Hibernate.class).toInstance(hibernateProps);
		
		bind(WebApplication.class).to(WicketConfig.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return Plugin.class;
	}

}
