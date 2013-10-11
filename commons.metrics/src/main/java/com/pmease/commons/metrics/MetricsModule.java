package com.pmease.commons.metrics;

import javax.inject.Singleton;

import com.codahale.metrics.MetricRegistry;
import com.pmease.commons.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class MetricsModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(MetricRegistry.class).toProvider(MetricRegistryProvider.class).in(Singleton.class);
	}

}
