package com.pmease.gitplex.core.manager;

import java.util.List;

import com.google.inject.ImplementedBy;
import com.pmease.commons.util.init.ManualConfig;
import com.pmease.gitplex.core.manager.impl.DefaultDataManager;

@ImplementedBy(DefaultDataManager.class)
public interface DataManager {

	List<ManualConfig> init();
	
}
