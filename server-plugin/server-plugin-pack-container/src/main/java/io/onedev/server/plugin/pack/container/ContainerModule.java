package io.onedev.server.plugin.pack.container;

import com.google.common.collect.Sets;
import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.server.OneDev;
import io.onedev.server.jetty.ServletConfigurator;
import io.onedev.server.model.support.PackSupport;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.Collection;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ContainerModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();		
		
		contribute(ImplementationProvider.class, new ImplementationProvider() {
			@Override
			public Class<?> getAbstractClass() {
				return PackSupport.class;
			}

			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(ContainerPackSupport.class);
			}
			
		});
		
		bind(ContainerServlet.class);
		contribute(ServletConfigurator.class, context -> context.addServlet(
				new ServletHolder(OneDev.getInstance(ContainerServlet.class)), 
				ContainerServlet.PATH + "/*"));
	}

}
