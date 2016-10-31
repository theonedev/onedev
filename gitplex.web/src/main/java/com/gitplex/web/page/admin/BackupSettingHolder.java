package com.gitplex.web.page.admin;

import java.io.Serializable;

import com.gitplex.core.setting.BackupSetting;
import com.gitplex.commons.wicket.editable.annotation.Editable;

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
