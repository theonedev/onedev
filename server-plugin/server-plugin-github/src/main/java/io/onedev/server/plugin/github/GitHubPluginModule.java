package io.onedev.server.plugin.github;

import java.io.Serializable;
import java.util.Collection;

import org.apache.wicket.protocol.http.WebApplication;

import com.google.common.collect.Lists;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.model.support.administration.sso.SsoConnectorContribution;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.DynamicPathPageMapper;
import io.onedev.server.web.page.layout.AdministrationSettingContribution;
import io.onedev.server.web.page.project.imports.ProjectImporter;
import io.onedev.server.web.page.project.imports.ProjectImporterContribution;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GitHubPluginModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(AdministrationSettingContribution.class, new AdministrationSettingContribution() {
			
			@Override
			public Class<? extends Serializable> getSettingClass() {
				return GitHubSetting.class;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(WebApplication application) {
				application.mount(new DynamicPathPageMapper(GitHubCallbackPage.MOUNT_PATH, GitHubCallbackPage.class));
			}
			
		});				
		
		contribute(ProjectImporterContribution.class, new ProjectImporterContribution() {
			
			@Override
			public Collection<ProjectImporter> getImporters() {
				Collection<ProjectImporter> importers = Lists.newArrayList();
				GitHubSetting setting = getSettingManager().getContributedSetting(GitHubSetting.class);
				if (setting != null) 
					importers.add(new GitHubImporter(setting));
				return importers;
			}
			
		});
		
		contribute(SsoConnectorContribution.class, new SsoConnectorContribution() {
			
			@Override
			public Collection<SsoConnector> getSsoConnectors() {
				Collection<SsoConnector> connectors = Lists.newArrayList();
				GitHubSetting setting = getSettingManager().getContributedSetting(GitHubSetting.class);
				if (setting != null) 
					connectors.add(new GitHubConnector(setting));
				return connectors;
			}
			
		});
		
	}
	
	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}
	
}
