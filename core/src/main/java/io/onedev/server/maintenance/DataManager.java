package io.onedev.server.maintenance;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.commons.utils.init.ManualConfig;
import io.onedev.server.model.support.setting.BackupSetting;

public interface DataManager {

	List<ManualConfig> init();
	
	void scheduleBackup(@Nullable BackupSetting backupSetting);
	
}
