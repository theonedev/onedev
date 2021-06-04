package io.onedev.server.web.page.layout;

import java.io.Serializable;

import io.onedev.commons.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface AdministrationSettingContribution {

	Class<? extends Serializable> getSettingClass();
	
}
