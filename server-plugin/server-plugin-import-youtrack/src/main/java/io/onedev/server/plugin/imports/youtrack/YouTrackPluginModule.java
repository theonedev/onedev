package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.Collection;

import com.google.common.collect.Lists;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.server.web.page.project.imports.ProjectImporter;
import io.onedev.server.web.page.project.imports.ProjectImporterContribution;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class YouTrackPluginModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ProjectImporterContribution.class, new ProjectImporterContribution() {

			@Override
			public Collection<ProjectImporter<? extends Serializable, ? extends Serializable>> getImporters() {
				return Lists.newArrayList(new YouTrackImporter());
			}
			
		});
	}

}
