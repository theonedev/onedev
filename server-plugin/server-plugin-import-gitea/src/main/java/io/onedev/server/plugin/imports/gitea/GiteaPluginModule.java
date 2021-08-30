package io.onedev.server.plugin.imports.gitea;

import java.io.Serializable;
import java.util.Collection;

import com.google.common.collect.Lists;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.imports.IssueImporter;
import io.onedev.server.imports.IssueImporterContribution;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.imports.ProjectImporterContribution;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GiteaPluginModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ProjectImporterContribution.class, new ProjectImporterContribution() {

			@Override
			public Collection<ProjectImporter<? extends Serializable, ? extends Serializable, ? extends Serializable>> getImporters() {
				return Lists.newArrayList(new GiteaProjectImporter());
			}

			@Override
			public int getOrder() {
				return 300;
			}
			
		});
		
		contribute(IssueImporterContribution.class, new IssueImporterContribution() {

			@Override
			public Collection<IssueImporter<? extends Serializable, ? extends Serializable, ? extends Serializable>> getImporters() {
				return Lists.newArrayList(new GiteaIssueImporter());
			}

			@Override
			public int getOrder() {
				return 300;
			}
			
		});
		
	}

}
