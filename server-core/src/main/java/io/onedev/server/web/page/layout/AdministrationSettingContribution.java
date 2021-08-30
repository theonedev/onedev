package io.onedev.server.web.page.layout;

import java.util.List;

import io.onedev.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface AdministrationSettingContribution {

	List<Class<? extends ContributedAdministrationSetting>> getSettingClasses();
	
}
