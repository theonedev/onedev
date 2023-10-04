package io.onedev.server.plugin.notification.ntfy;

import java.util.List;

import com.beust.jcommander.internal.Lists;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;
import io.onedev.server.web.page.project.setting.ProjectSettingContribution;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class NtfyModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ProjectSettingContribution.class, new ProjectSettingContribution() {
			
			@SuppressWarnings("unchecked")
			@Override
			public List<Class<? extends ContributedProjectSetting>> getSettingClasses() {
				return Lists.newArrayList(NtfyNotificationSetting.class);
			}
			
		});
		bind(NtfyNotificationManager.class);
	}

}
