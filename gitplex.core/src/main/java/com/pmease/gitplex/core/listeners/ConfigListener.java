package com.pmease.gitplex.core.listeners;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.model.Config;

@ExtensionPoint
public interface ConfigListener {
	
	void onSave(Config config);
}
