package com.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.core.entity.Config;
import com.gitplex.core.entity.Config.Key;
import com.gitplex.core.manager.ConfigManager;
import com.gitplex.core.manager.DataManager;
import com.gitplex.core.setting.BackupSetting;
import com.gitplex.core.setting.MailSetting;
import com.gitplex.core.setting.SecuritySetting;
import com.gitplex.core.setting.SystemSetting;
import com.google.common.base.Preconditions;
import com.gitplex.commons.hibernate.Sessional;
import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.dao.EntityCriteria;

@Singleton
public class DefaultConfigManager extends AbstractEntityManager<Config> implements ConfigManager {
	
	private final DataManager dataManager;
	
	private volatile Long systemSettingConfigId;
	
	private volatile Long mailSettingConfigId;
	
	private volatile Long backupSettingConfigId;
	
	private volatile Long securitySettingConfigId;
	
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

	@Sessional
	@Override
	public SecuritySetting getSecuritySetting() {
        Config config;
        if (securitySettingConfigId == null) {
    		config = getConfig(Key.SECURITY);
    		Preconditions.checkNotNull(config);
    		securitySettingConfigId = config.getId();
        } else {
            config = load(securitySettingConfigId);
        }
        return (SecuritySetting) config.getSetting();
	}

	@Transactional
	@Override
	public void saveSecuritySetting(SecuritySetting securitySetting) {
		Config config = getConfig(Key.SECURITY);
		if (config == null) {
			config = new Config();
			config.setKey(Key.SECURITY);
		}
		config.setSetting(securitySetting);
		dao.persist(config);
	}
	
}
