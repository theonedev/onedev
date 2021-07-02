package io.onedev.server.web.page.project.setting;

import java.util.List;

import io.onedev.commons.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface ProjectSettingContribution {

	List<Class<? extends ContributedProjectSetting>> getSettingClasses();
	
}
