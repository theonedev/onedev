package io.onedev.server.plugin.imports.bitbucketcloud;

import java.util.Collection;

import com.google.common.collect.Lists;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.imports.ProjectImporterContribution;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */

public class BitbucketModule extends AbstractPluginModule {
	
	static final String NAME = "Bitbucket Cloud";
	
	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ProjectImporterContribution.class, new ProjectImporterContribution() {

			@Override
			public Collection<ProjectImporter> getImporters() {
				return Lists.newArrayList(new BitbucketProjectImporter());
			}

			@Override
			public int getOrder() {
				return 250;
			}
			
		});
		
	}

}
