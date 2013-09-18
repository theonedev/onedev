package com.pmease.gitop.core.manager;

import java.util.List;

import com.google.inject.ImplementedBy;
import com.pmease.commons.util.init.ManualConfig;
import com.pmease.gitop.core.manager.impl.DefaultDataManager;

@ImplementedBy(DefaultDataManager.class)
public interface DataManager {

	List<ManualConfig> init();
	
}
