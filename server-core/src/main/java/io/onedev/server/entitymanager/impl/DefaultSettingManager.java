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
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.GlobalProjectSetting;
import io.onedev.server.model.support.administration.GlobalPullRequestSetting;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.administration.MailSetting;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.administration.SshSetting;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultSettingManager extends BaseEntityManager<Setting> implements SettingManager {
	
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
	
	private volatile Long projectSettingId;

    private volatile Long sshSettingId;
    
    private volatile Long ssoConnectorsId;
	
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
	public void saveIssueSetting(GlobalIssueSetting issueSetting) {
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
	public GlobalIssueSetting getIssueSetting() {
        Setting setting;
        if (issueSettingId == null) {
    		setting = getSetting(Key.ISSUE);
    		Preconditions.checkNotNull(setting);
    		issueSettingId = setting.getId();
        } else {
            setting = load(issueSettingId);
        }
        return (GlobalIssueSetting) setting.getValue();
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
    		setting = getSetting(Key.GROOVY_SCRIPTS);
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
		Setting setting = getSetting(Key.GROOVY_SCRIPTS);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.GROOVY_SCRIPTS);
		}
		setting.setValue((Serializable) jobScripts);
		dao.persist(setting);
	}
	
	@Sessional
	@Override
	public GlobalPullRequestSetting getPullRequestSetting() {
        Setting setting;
        if (pullRequestSettingId == null) {
    		setting = getSetting(Key.PULL_REQUEST);
    		Preconditions.checkNotNull(setting);
    		pullRequestSettingId = setting.getId();
        } else {
            setting = load(pullRequestSettingId);
        }
        return (GlobalPullRequestSetting)setting.getValue();
	}

	@Transactional
	@Override
	public void savePullRequestSetting(GlobalPullRequestSetting pullRequestSetting) {
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
	public GlobalBuildSetting getBuildSetting() {
        Setting setting;
        if (buildSettingId == null) {
    		setting = getSetting(Key.BUILD);
    		Preconditions.checkNotNull(setting);
    		buildSettingId = setting.getId();
        } else {
            setting = load(buildSettingId);
        }
        return (GlobalBuildSetting)setting.getValue();
	}

	@Transactional
	@Override
	public void saveBuildSetting(GlobalBuildSetting buildSetting) {
		Setting setting = getSetting(Key.BUILD);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.BUILD);
		}
		setting.setValue(buildSetting);
		dao.persist(setting);
	}
	
	@Sessional
	@Override
	public GlobalProjectSetting getProjectSetting() {
        Setting setting;
        if (projectSettingId == null) {
    		setting = getSetting(Key.PROJECT);
    		Preconditions.checkNotNull(setting);
    		projectSettingId = setting.getId();
        } else {
            setting = load(projectSettingId);
        }
        return (GlobalProjectSetting)setting.getValue();
	}

	@Transactional
	@Override
	public void saveProjectSetting(GlobalProjectSetting projectSetting) {
		Setting setting = getSetting(Key.PROJECT);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.PROJECT);
		}
		setting.setValue(projectSetting);
		dao.persist(setting);
	}
	
	@Sessional
    @Override
    public SshSetting getSshSetting() {
        Setting setting;
        if (sshSettingId == null) {
            setting = getSetting(Key.SSH);
            Preconditions.checkNotNull(setting);
            sshSettingId = setting.getId();
        } else {
            setting = load(sshSettingId);
        }
        return (SshSetting)setting.getValue();
    }

    @Transactional
    @Override
    public void saveSshSetting(SshSetting sshSetting) {
        Setting setting = getSetting(Key.SSH);
        if (setting == null) {
            setting = new Setting();
            setting.setKey(Key.SSH);
        }
        setting.setValue(sshSetting);
        dao.persist(setting);
    }

	@Sessional
	@SuppressWarnings("unchecked")
	@Override
	public List<SsoConnector> getSsoConnectors() {
        Setting setting;
        if (ssoConnectorsId == null) {
    		setting = getSetting(Key.SSO_CONNECTORS);
    		Preconditions.checkNotNull(setting);
    		ssoConnectorsId = setting.getId();
        } else {
            setting = load(ssoConnectorsId);
        }
        return (List<SsoConnector>) setting.getValue();
	}

	@Transactional
	@Override
	public void saveSsoConnectors(List<SsoConnector> ssoProviders) {
		Setting setting = getSetting(Key.SSO_CONNECTORS);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.SSO_CONNECTORS);
		}
		setting.setValue((Serializable) ssoProviders);
		dao.persist(setting);
	}
	
}
