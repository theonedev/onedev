package com.pmease.gitop.core.manager;

import java.util.List;

import com.google.inject.ImplementedBy;
import com.pmease.gitop.core.ManualConfig;
import com.pmease.gitop.core.manager.impl.DefaultInitManager;

@ImplementedBy(DefaultInitManager.class)
public interface InitManager {

	List<ManualConfig> init();
	
}
