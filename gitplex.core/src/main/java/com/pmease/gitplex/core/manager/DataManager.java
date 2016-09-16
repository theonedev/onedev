package com.pmease.gitplex.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.util.init.ManualConfig;
import com.pmease.gitplex.core.setting.BackupSetting;

public interface DataManager {

	List<ManualConfig> init();
	
	void scheduleBackup(@Nullable BackupSetting backupSetting);
	
}
