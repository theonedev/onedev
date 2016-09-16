package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Config;
import com.pmease.gitplex.core.entity.Config.Key;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.DataManager;
import com.pmease.gitplex.core.setting.BackupSetting;
import com.pmease.gitplex.core.setting.MailSetting;
import com.pmease.gitplex.core.setting.SystemSetting;

@Singleton
public class DefaultConfigManager extends AbstractEntityManager<Config> implements ConfigManager {
	
	private final DataManager dataManager;
	
	private volatile Long systemSettingConfigId;
	
	private volatile Long mailSettingConfigId;
	
	private volatile Long backupSettingConfigId;
	
	@Inject
	public DefaultConfigManager(Dao dao, DataManager dataManager) {
		super(dao);
		this.dataManager = dataManager;
	}

	@Sessional
	@Override
	public SystemSetting getSystemSetting() {
        Config config;
        if (systemSettingConfigId == null) {
    		config = getConfig(Key.SYSTEM);
    		Preconditions.checkNotNull(config);
            systemSettingConfigId = config.getId();
        } else {
            config = load(systemSettingConfigId);
        }
        SystemSetting setting = (SystemSetting) config.getSetting();
        Preconditions.checkNotNull(setting);
        return setting;
	}

	@Transactional
	@Override
	public void saveSystemSetting(SystemSetting systemSetting) {
		Preconditions.checkNotNull(systemSetting);
		
		Config config = getConfig(Key.SYSTEM);
		if (config == null) {
			config = new Config();
			config.setKey(Key.SYSTEM);
		}
		config.setSetting(systemSetting);
		dao.persist(config);
	}

	@Sessional
	@Override
	public Config getConfig(Key key) {
		return find(EntityCriteria.of(Config.class).add(Restrictions.eq("key", key)));
	}

	@Sessional
	@Override
	public MailSetting getMailSetting() {
        Config config;
        if (mailSettingConfigId == null) {
    		config = getConfig(Key.MAIL);
    		Preconditions.checkNotNull(config);
    		mailSettingConfigId = config.getId();
        } else {
            config = load(mailSettingConfigId);
        }
        return (MailSetting) config.getSetting();
	}

	@Transactional
	@Override
	public void saveMailSetting(MailSetting mailSetting) {
		Config config = getConfig(Key.MAIL);
		if (config == null) {
			config = new Config();
			config.setKey(Key.MAIL);
		}
		config.setSetting(mailSetting);
		dao.persist(config);
	}

	@Sessional
	@Override
	public BackupSetting getBackupSetting() {
        Config config;
        if (backupSettingConfigId == null) {
    		config = getConfig(Key.BACKUP);
    		Preconditions.checkNotNull(config);
    		backupSettingConfigId = config.getId();
        } else {
            config = load(backupSettingConfigId);
        }
        return (BackupSetting) config.getSetting();
	}

	@Transactional
	@Override
	public void saveBackupSetting(BackupSetting backupSetting) {
		Config config = getConfig(Key.BACKUP);
		if (config == null) {
			config = new Config();
			config.setKey(Key.BACKUP);
		}
		config.setSetting(backupSetting);
		dao.persist(config);
		dataManager.scheduleBackup(backupSetting);
	}

}
