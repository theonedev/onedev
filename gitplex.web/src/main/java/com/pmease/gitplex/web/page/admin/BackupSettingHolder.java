package com.pmease.gitplex.web.page.admin;

import java.io.Serializable;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.setting.BackupSetting;

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
