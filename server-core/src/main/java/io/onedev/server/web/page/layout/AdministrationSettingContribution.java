package io.onedev.server.web.page.layout;

import java.io.Serializable;
import java.util.List;

import io.onedev.commons.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface AdministrationSettingContribution {

	List<Class<? extends Serializable>> getSettingClasses();
	
}
