package com.pmease.gitplex.core.extensionpoint;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.entity.Config;

@ExtensionPoint
public interface ConfigListener {
	
	void onSave(Config config);
}
