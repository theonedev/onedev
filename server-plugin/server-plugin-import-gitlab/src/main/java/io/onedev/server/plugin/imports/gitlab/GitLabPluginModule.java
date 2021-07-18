package io.onedev.server.plugin.imports.gitlab;

import java.io.Serializable;
import java.util.Collection;

import com.google.common.collect.Lists;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.server.web.page.project.imports.ProjectImporter;
import io.onedev.server.web.page.project.imports.ProjectImporterContribution;
import io.onedev.server.web.page.project.issues.imports.IssueImporter;
import io.onedev.server.web.page.project.issues.imports.IssueImporterContribution;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GitLabPluginModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		contribute(ProjectImporterContribution.class, new ProjectImporterContribution() {

			@Override
			public Collection<ProjectImporter<? extends Serializable, ? extends Serializable>> getImporters() {
				return Lists.newArrayList(new GitLabProjectImporter());
			}

			@Override
			public int getOrder() {
				return 200;
			}
			
		});
		
		contribute(IssueImporterContribution.class, new IssueImporterContribution() {

			@Override
			public Collection<IssueImporter<? extends Serializable, ? extends Serializable>> getImporters() {
				return Lists.newArrayList(new GitLabIssueImporter());
			}

			@Override
			public int getOrder() {
				return 200;
			}
			
		});
	}
	
}
