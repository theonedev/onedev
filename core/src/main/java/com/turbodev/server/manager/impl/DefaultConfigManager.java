package com.turbodev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.turbodev.utils.license.LicenseDetail;
import com.google.common.base.Preconditions;
import com.turbodev.server.manager.ConfigManager;
import com.turbodev.server.manager.DataManager;
import com.turbodev.server.migration.VersionedDocument;
import com.turbodev.server.model.Config;
import com.turbodev.server.model.Config.Key;
import com.turbodev.server.model.support.setting.BackupSetting;
import com.turbodev.server.model.support.setting.MailSetting;
import com.turbodev.server.model.support.setting.SecuritySetting;
import com.turbodev.server.model.support.setting.SystemSetting;
import com.turbodev.server.persistence.annotation.Sessional;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.persistence.dao.EntityCriteria;
import com.turbodev.server.security.authenticator.Authenticator;

@Singleton
public class DefaultConfigManager extends AbstractEntityManager<Config> implements ConfigManager {
	
	private final DataManager dataManager;
	
	private volatile Long systemSettingConfigId;
	
	private volatile Long mailSettingConfigId;
	
	private volatile Long backupSettingConfigId;
	
	private volatile Long securitySettingConfigId;
	
	private volatile Long authenticatorConfigId;
	
	private volatile Long licenseConfigId;
	
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
	
	@Sessional
	@Override
	public Authenticator getAuthenticator() {
        Config config;
        if (authenticatorConfigId == null) {
    		config = getConfig(Key.AUTHENTICATOR);
    		Preconditions.checkNotNull(config);
    		authenticatorConfigId = config.getId();
        } else {
            config = load(authenticatorConfigId);
        }
        VersionedDocument dom = (VersionedDocument) config.getSetting();
        if (dom != null)
        	return (Authenticator) dom.toBean();
        else
        	return null;
	}

	@Transactional
	@Override
	public void saveAuthenticator(Authenticator authenticator) {
		Config config = getConfig(Key.AUTHENTICATOR);
		if (config == null) {
			config = new Config();
			config.setKey(Key.AUTHENTICATOR);
		}
		if (authenticator != null)
			config.setSetting(VersionedDocument.fromBean(authenticator));
		else
			config.setSetting(null);
		dao.persist(config);
	}

	@Sessional
	@Override
	public LicenseDetail getLicense() {
        Config config;
        if (licenseConfigId == null) {
    		config = getConfig(Key.LICENSE);
    		Preconditions.checkNotNull(config);
            licenseConfigId = config.getId();
        } else {
            config = load(licenseConfigId);
        }
        return (LicenseDetail) config.getSetting(); 
	}

	@Transactional
	@Override
	public void saveLicense(LicenseDetail license) {
		Config config = getConfig(Key.LICENSE);
		if (config == null) {
			config = new Config();
			config.setKey(Key.LICENSE);
		}
		config.setSetting(license);
		dao.persist(config);
	}
	
}
