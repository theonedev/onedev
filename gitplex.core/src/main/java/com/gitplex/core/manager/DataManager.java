package com.gitplex.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.gitplex.core.setting.BackupSetting;
import com.gitplex.commons.util.init.ManualConfig;

public interface DataManager {

	List<ManualConfig> init();
	
	void scheduleBackup(@Nullable BackupSetting backupSetting);
	
}
