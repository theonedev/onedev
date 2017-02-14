package com.gitplex.commons.schedule;

import com.gitplex.calla.loader.AbstractPlugin;
import com.gitplex.calla.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ScheduleModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(TaskScheduler.class).to(DefaultTaskScheduler.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return SchedulePlugin.class;
	}

}
