package io.onedev.server.plugin.imports.gitlab;

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
public class GitLabModule extends AbstractPluginModule {

	static final String NAME = "GitLab";
	
	protected void configure() {
		super.configure();
		
		contribute(ProjectImporterContribution.class, new ProjectImporterContribution() {

			@Override
			public Collection<ProjectImporter> getImporters() {
				return Lists.newArrayList(new GitLabProjectImporter());
			}

			@Override
			public int getOrder() {
				return 200;
			}
			
		});
		
		contribute(IssueImporterContribution.class, new IssueImporterContribution() {

			@Override
			public Collection<IssueImporter> getImporters() {
				return Lists.newArrayList(new GitLabIssueImporter());
			}

			@Override
			public int getOrder() {
				return 200;
			}
			
		});
	}
	
}
