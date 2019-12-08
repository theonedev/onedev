package io.onedev.server.maintenance;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.support.administration.BackupSetting;
import io.onedev.server.util.init.ManualConfig;

public interface DataManager {

	List<ManualConfig> init();
	
	void scheduleBackup(@Nullable BackupSetting backupSetting);
	
}
