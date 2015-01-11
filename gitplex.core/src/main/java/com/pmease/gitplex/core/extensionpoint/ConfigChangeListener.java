package com.pmease.gitplex.core.extensionpoint;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ConfigChangeListener {
	
	void systemSettingChanged();
	
	void mailSettingChanged();
	
	void qosSettingChanged();
	
}
