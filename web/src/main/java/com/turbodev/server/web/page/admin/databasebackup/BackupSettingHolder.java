package com.turbodev.server.web.page.admin.databasebackup;

import java.io.Serializable;

import com.turbodev.server.model.support.setting.BackupSetting;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable
public class BackupSettingHolder implements Serializable {

	private static final long serialVersionUID = 1L;

	private BackupSetting backupSetting;

	@Editable(name="Enable Auto Backup")
	public BackupSetting getBackupSetting() {
		return backupSetting;
	}

	public void setBackupSetting(BackupSetting backupSetting) {
		this.backupSetting = backupSetting;
	}
	
}
