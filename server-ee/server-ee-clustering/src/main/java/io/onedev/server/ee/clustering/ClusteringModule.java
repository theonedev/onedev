package io.onedev.server.ee.clustering;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.cluster.ClusterManager;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ClusteringModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(ClusterManager.class).to(EEClusterManager.class);
	}

}
