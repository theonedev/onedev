package io.onedev.server.entitymanager.impl;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;

import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.maintenance.DataManager;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.support.administration.BackupSetting;
import io.onedev.server.model.support.administration.BuildSetting;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.administration.IssueSetting;
import io.onedev.server.model.support.administration.MailSetting;
import io.onedev.server.model.support.administration.PullRequestSetting;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultSettingManager extends AbstractEntityManager<Setting> implements SettingManager {
	
	private final DataManager dataManager;
	
	private volatile Long systemSettingId;
	
	private volatile Long mailSettingId;
	
	private volatile Long backupSettingId;
	
	private volatile Long securitySettingId;
	
	private volatile Long issueSettingId;
	
	private volatile Long authenticatorId;
	
	private volatile Long jobExecutorsId;
	
	private volatile Long jobScriptsId;
	
	private volatile Long pullRequestSettingId;
	
	private volatile Long buildSettingId;
	
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
	public void saveIssueSetting(IssueSetting issueSetting) {
		Setting setting = getSetting(Key.ISSUE);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.ISSUE);
		}
		setting.setValue(issueSetting);
		dao.persist(setting);
	}
	
	@Sessional
	@Override
	public IssueSetting getIssueSetting() {
        Setting setting;
        if (issueSettingId == null) {
    		setting = getSetting(Key.ISSUE);
    		Preconditions.checkNotNull(setting);
    		issueSettingId = setting.getId();
        } else {
            setting = load(issueSettingId);
        }
        return (IssueSetting) setting.getValue();
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
        return (Authenticator) setting.getValue();
	}

	@Transactional
	@Override
	public void saveAuthenticator(Authenticator authenticator) {
		Setting setting = getSetting(Key.AUTHENTICATOR);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.AUTHENTICATOR);
		}
		setting.setValue(authenticator);
		dao.persist(setting);
	}

	@Sessional
	@SuppressWarnings("unchecked")
	@Override
	public List<JobExecutor> getJobExecutors() {
        Setting setting;
        if (jobExecutorsId == null) {
    		setting = getSetting(Key.JOB_EXECUTORS);
    		Preconditions.checkNotNull(setting);
    		jobExecutorsId = setting.getId();
        } else {
            setting = load(jobExecutorsId);
        }
        return (List<JobExecutor>) setting.getValue();
	}

	@Transactional
	@Override
	public void saveJobExecutors(List<JobExecutor> jobExecutors) {
		Setting setting = getSetting(Key.JOB_EXECUTORS);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.JOB_EXECUTORS);
		}
		setting.setValue((Serializable) jobExecutors);
		dao.persist(setting);
	}

	@Sessional
	@SuppressWarnings("unchecked")
	@Override
	public List<GroovyScript> getGroovyScripts() {
        Setting setting;
        if (jobScriptsId == null) {
    		setting = getSetting(Key.JOB_SCRIPTS);
    		Preconditions.checkNotNull(setting);
    		jobScriptsId = setting.getId();
        } else {
            setting = load(jobScriptsId);
        }
        return (List<GroovyScript>) setting.getValue();
	}

	@Transactional
	@Override
	public void saveGroovyScripts(List<GroovyScript> jobScripts) {
		Setting setting = getSetting(Key.JOB_SCRIPTS);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.JOB_SCRIPTS);
		}
		setting.setValue((Serializable) jobScripts);
		dao.persist(setting);
	}
	
	@Sessional
	@Override
	public PullRequestSetting getPullRequestSetting() {
        Setting setting;
        if (pullRequestSettingId == null) {
    		setting = getSetting(Key.PULL_REQUEST);
    		Preconditions.checkNotNull(setting);
    		pullRequestSettingId = setting.getId();
        } else {
            setting = load(pullRequestSettingId);
        }
        return (PullRequestSetting)setting.getValue();
	}

	@Transactional
	@Override
	public void savePullRequestSetting(PullRequestSetting pullRequestSetting) {
		Setting setting = getSetting(Key.PULL_REQUEST);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.PULL_REQUEST);
		}
		setting.setValue(pullRequestSetting);
		dao.persist(setting);
	}
	
	@Sessional
	@Override
	public BuildSetting getBuildSetting() {
        Setting setting;
        if (buildSettingId == null) {
    		setting = getSetting(Key.BUILD);
    		Preconditions.checkNotNull(setting);
    		buildSettingId = setting.getId();
        } else {
            setting = load(buildSettingId);
        }
        return (BuildSetting)setting.getValue();
	}

	@Transactional
	@Override
	public void saveBuildSetting(BuildSetting buildSetting) {
		Setting setting = getSetting(Key.BUILD);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.BUILD);
		}
		setting.setValue(buildSetting);
		dao.persist(setting);
	}
	
}
