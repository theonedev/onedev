package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.Collection;

import com.google.common.collect.Lists;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.server.web.page.project.issues.imports.IssueImporter;
import io.onedev.server.web.page.project.issues.imports.IssueImporterContribution;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class YouTrackPluginModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here		
		contribute(IssueImporterContribution.class, new IssueImporterContribution() {

			@Override
			public Collection<IssueImporter<? extends Serializable, ? extends Serializable>> getImporters() {
				return Lists.newArrayList(new YouTrackIssueImporter());
			}

			@Override
			public int getOrder() {
				return 300;
			}
			
		});
	}

}
