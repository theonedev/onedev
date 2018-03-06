package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;

import io.onedev.server.manager.ConfigManager;
import io.onedev.server.manager.DataManager;
import io.onedev.server.migration.VersionedDocument;
import io.onedev.server.model.Config;
import io.onedev.server.model.Config.Key;
import io.onedev.server.model.support.setting.BackupSetting;
import io.onedev.server.model.support.setting.MailSetting;
import io.onedev.server.model.support.setting.SecuritySetting;
import io.onedev.server.model.support.setting.SystemSetting;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.authenticator.Authenticator;
import io.onedev.utils.license.LicenseDetail;

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
