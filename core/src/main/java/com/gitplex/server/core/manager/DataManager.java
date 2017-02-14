package com.gitplex.server.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.gitplex.commons.util.init.ManualConfig;
import com.gitplex.server.core.setting.BackupSetting;

public interface DataManager {

	List<ManualConfig> init();
	
	void scheduleBackup(@Nullable BackupSetting backupSetting);
	
}
