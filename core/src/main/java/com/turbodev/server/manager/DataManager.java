package com.turbodev.server.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.turbodev.utils.init.ManualConfig;
import com.turbodev.server.model.support.setting.BackupSetting;

public interface DataManager {

	List<ManualConfig> init();
	
	void scheduleBackup(@Nullable BackupSetting backupSetting);
	
}
