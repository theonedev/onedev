package com.pmease.gitplex.core.extensionpoint;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface LifecycleListener {
	void systemStarting();
	
	void systemStarted();
	
	void systemStopping();
	
	void systemStopped();
}
