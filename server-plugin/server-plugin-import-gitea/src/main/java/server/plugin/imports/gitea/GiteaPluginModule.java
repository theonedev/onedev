package server.plugin.imports.gitea;

import java.io.Serializable;
import java.util.Collection;

import com.google.common.collect.Lists;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.server.imports.IssueImporter2;
import io.onedev.server.imports.IssueImporterContribution2;
import io.onedev.server.imports.ProjectImporter2;
import io.onedev.server.imports.ProjectImporterContribution2;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GiteaPluginModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ProjectImporterContribution2.class, new ProjectImporterContribution2() {

			@Override
			public Collection<ProjectImporter2<? extends Serializable, ? extends Serializable, ? extends Serializable>> getImporters() {
				return Lists.newArrayList(new GiteaProjectImporter());
			}

			@Override
			public int getOrder() {
				return 500;
			}
			
		});
		
		contribute(IssueImporterContribution2.class, new IssueImporterContribution2() {

			@Override
			public Collection<IssueImporter2<? extends Serializable, ? extends Serializable, ? extends Serializable>> getImporters() {
				return Lists.newArrayList(new GiteaIssueImporter());
			}

			@Override
			public int getOrder() {
				return 500;
			}
			
		});
		
	}

}
