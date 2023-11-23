package io.onedev.server.ee.pack.container;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.OneDev;
import io.onedev.server.jetty.ServletConfigurator;
import io.onedev.server.pack.PackSupport;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ContainerModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();		
		
		bind(ContainerServlet.class);
		contribute(ServletConfigurator.class, context -> context.addServlet(
				new ServletHolder(OneDev.getInstance(ContainerServlet.class)), 
				ContainerServlet.PATH + "/*"));
		
		bind(ContainerPackSupport.class);
		contribute(PackSupport.class, ContainerPackSupport.class);
	}

}
