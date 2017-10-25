package com.gitplex.server.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.gitplex.server.model.support.setting.BackupSetting;
import com.gitplex.utils.init.ManualConfig;

public interface DataManager {

	List<ManualConfig> init();
	
	void scheduleBackup(@Nullable BackupSetting backupSetting);
	
}
