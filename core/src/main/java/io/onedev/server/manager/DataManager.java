package io.onedev.server.manager;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.support.setting.BackupSetting;
import io.onedev.utils.init.ManualConfig;

public interface DataManager {

	List<ManualConfig> init();
	
	void scheduleBackup(@Nullable BackupSetting backupSetting);
	
}
