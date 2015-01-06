package com.pmease.gitplex.core.extensionpoint;


public interface ConfigChangeListener {
	
	void systemSettingChanged();
	
	void mailSettingChanged();
	
	void qosSettingChanged();
	
}
