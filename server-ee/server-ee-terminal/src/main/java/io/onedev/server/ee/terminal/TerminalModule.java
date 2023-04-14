package io.onedev.server.ee.terminal;

import org.apache.wicket.protocol.http.WebApplication;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.terminal.TerminalManager;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.BasePageMapper;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class TerminalModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(TerminalManager.class).to(EETerminalManager.class);
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(WebApplication application) {
				application.mount(new BasePageMapper("projects/${project}/builds/${build}/terminal", BuildTerminalPage.class));
			}
			
		});		
	}

}
