package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;

import io.onedev.server.manager.SettingManager;
import io.onedev.server.manager.DataManager;
import io.onedev.server.migration.VersionedDocument;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
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
public class DefaultSettingManager extends AbstractEntityManager<Setting> implements SettingManager {
	
	private final DataManager dataManager;
	
	private volatile Long systemSettingId;
	
	private volatile Long mailSettingId;
	
	private volatile Long backupSettingId;
	
	private volatile Long securitySettingId;
	
	private volatile Long authenticatorId;
	
	private volatile Long licenseId;
	
	@Inject
	public DefaultSettingManager(Dao dao, DataManager dataManager) {
		super(dao);
		this.dataManager = dataManager;
	}

	@Sessional
	@Override
	public SystemSetting getSystemSetting() {
        Setting setting;
        if (systemSettingId == null) {
    		setting = getSetting(Key.SYSTEM);
    		Preconditions.checkNotNull(setting);
            systemSettingId = setting.getId();
        } else {
            setting = load(systemSettingId);
        }
        SystemSetting value = (SystemSetting) setting.getValue();
        Preconditions.checkNotNull(value);
        return value;
	}

	@Transactional
	@Override
	public void saveSystemSetting(SystemSetting systemSetting) {
		Preconditions.checkNotNull(systemSetting);
		
		Setting setting = getSetting(Key.SYSTEM);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.SYSTEM);
		}
		setting.setValue(systemSetting);
		dao.persist(setting);
	}

	@Sessional
	@Override
	public Setting getSetting(Key key) {
		return find(EntityCriteria.of(Setting.class).add(Restrictions.eq("key", key)));
	}

	@Sessional
	@Override
	public MailSetting getMailSetting() {
        Setting setting;
        if (mailSettingId == null) {
    		setting = getSetting(Key.MAIL);
    		Preconditions.checkNotNull(setting);
    		mailSettingId = setting.getId();
        } else {
            setting = load(mailSettingId);
        }
        return (MailSetting) setting.getValue();
	}

	@Transactional
	@Override
	public void saveMailSetting(MailSetting mailSetting) {
		Setting setting = getSetting(Key.MAIL);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.MAIL);
		}
		setting.setValue(mailSetting);
		dao.persist(setting);
	}

	@Sessional
	@Override
	public BackupSetting getBackupSetting() {
        Setting setting;
        if (backupSettingId == null) {
    		setting = getSetting(Key.BACKUP);
    		Preconditions.checkNotNull(setting);
    		backupSettingId = setting.getId();
        } else {
            setting = load(backupSettingId);
        }
        return (BackupSetting) setting.getValue();
	}

	@Transactional
	@Override
	public void saveBackupSetting(BackupSetting backupSetting) {
		Setting setting = getSetting(Key.BACKUP);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.BACKUP);
		}
		setting.setValue(backupSetting);
		dao.persist(setting);
		dataManager.scheduleBackup(backupSetting);
	}

	@Sessional
	@Override
	public SecuritySetting getSecuritySetting() {
        Setting setting;
        if (securitySettingId == null) {
    		setting = getSetting(Key.SECURITY);
    		Preconditions.checkNotNull(setting);
    		securitySettingId = setting.getId();
        } else {
            setting = load(securitySettingId);
        }
        return (SecuritySetting) setting.getValue();
	}

	@Transactional
	@Override
	public void saveSecuritySetting(SecuritySetting securitySetting) {
		Setting setting = getSetting(Key.SECURITY);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.SECURITY);
		}
		setting.setValue(securitySetting);
		dao.persist(setting);
	}
	
	@Sessional
	@Override
	public Authenticator getAuthenticator() {
        Setting setting;
        if (authenticatorId == null) {
    		setting = getSetting(Key.AUTHENTICATOR);
    		Preconditions.checkNotNull(setting);
    		authenticatorId = setting.getId();
        } else {
            setting = load(authenticatorId);
        }
        VersionedDocument dom = (VersionedDocument) setting.getValue();
        if (dom != null)
        	return (Authenticator) dom.toBean();
        else
        	return null;
	}

	@Transactional
	@Override
	public void saveAuthenticator(Authenticator authenticator) {
		Setting setting = getSetting(Key.AUTHENTICATOR);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.AUTHENTICATOR);
		}
		if (authenticator != null)
			setting.setValue(VersionedDocument.fromBean(authenticator));
		else
			setting.setValue(null);
		dao.persist(setting);
	}

	@Sessional
	@Override
	public LicenseDetail getLicense() {
        Setting setting;
        if (licenseId == null) {
    		setting = getSetting(Key.LICENSE);
    		Preconditions.checkNotNull(setting);
            licenseId = setting.getId();
        } else {
            setting = load(licenseId);
        }
        return (LicenseDetail) setting.getValue(); 
	}

	@Transactional
	@Override
	public void saveLicense(LicenseDetail license) {
		Setting setting = getSetting(Key.LICENSE);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.LICENSE);
		}
		setting.setValue(license);
		dao.persist(setting);
	}
	
}
