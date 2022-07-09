package io.onedev.server.entitymanager.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.maintenance.DataManager;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.support.administration.AgentSetting;
import io.onedev.server.model.support.administration.BackupSetting;
import io.onedev.server.model.support.administration.BrandingSetting;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.GlobalProjectSetting;
import io.onedev.server.model.support.administration.GlobalPullRequestSetting;
import io.onedev.server.model.support.administration.GpgSetting;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.administration.PerformanceSetting;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.administration.ServiceDeskSetting;
import io.onedev.server.model.support.administration.SshSetting;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.mailsetting.MailSetting;
import io.onedev.server.model.support.administration.notificationtemplate.NotificationTemplateSetting;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;

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
	
	private volatile Long notificationTemplateSettingId;
	
	private volatile Long serviceDeskSettingId;
	
	private volatile Long jobScriptsId;
	
	private volatile Long pullRequestSettingId;
	
	private volatile Long buildSettingId;
	
	private volatile Long projectSettingId;

	private volatile Long agentSettingId;
	
    private volatile Long sshSettingId;
    
    private volatile Long gpgSettingId;
    
    private volatile Long ssoConnectorsId;
    
    private volatile Long contributedSettingsId;
	
    private volatile Long performanceSettingId;
    
    private volatile Long brandingSettingId;
    
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
	public BrandingSetting getBrandingSetting() {
        Setting setting;
        if (brandingSettingId == null) {
    		setting = getSetting(Key.BRANDING);
    		Preconditions.checkNotNull(setting);
    		brandingSettingId = setting.getId();
        } else {
            setting = load(brandingSettingId);
        }
        return (BrandingSetting) setting.getValue();
	}

	@Transactional
	@Override
	public void saveBrandingSetting(BrandingSetting brandingSetting) {
		Setting setting = getSetting(Key.BRANDING);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.BRANDING);
		}
		setting.setValue(brandingSetting);
		dao.persist(setting);
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
	@Override
	public NotificationTemplateSetting getNotificationTemplateSetting() {
        Setting setting;
        if (notificationTemplateSettingId == null) {
    		setting = getSetting(Key.NOTIFICATION_TEMPLATE_SETTING);
    		Preconditions.checkNotNull(setting);
    		notificationTemplateSettingId = setting.getId();
        } else {
            setting = load(notificationTemplateSettingId);
        }
        return (NotificationTemplateSetting) setting.getValue();
	}

	@Transactional
	@Override
	public void saveNotificationTemplateSetting(NotificationTemplateSetting notificationTemplateSetting) {
		Setting setting = getSetting(Key.NOTIFICATION_TEMPLATE_SETTING);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.NOTIFICATION_TEMPLATE_SETTING);
		}
		setting.setValue((Serializable) notificationTemplateSetting);
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
	public AgentSetting getAgentSetting() {
        Setting setting;
        if (agentSettingId == null) {
    		setting = getSetting(Key.AGENT);
    		Preconditions.checkNotNull(setting);
    		agentSettingId = setting.getId();
        } else {
            setting = load(agentSettingId);
        }
        return (AgentSetting)setting.getValue();
	}

	@Transactional
	@Override
	public void saveAgentSetting(AgentSetting agentSetting) {
		Setting setting = getSetting(Key.AGENT);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.AGENT);
		}
		setting.setValue(agentSetting);
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
    @Override
    public PerformanceSetting getPerformanceSetting() {
        Setting setting;
        if (performanceSettingId == null) {
            setting = getSetting(Key.PERFORMANCE);
            Preconditions.checkNotNull(setting);
            performanceSettingId = setting.getId();
        } else {
            setting = load(performanceSettingId);
        }
        return (PerformanceSetting)setting.getValue();
    }

    @Transactional
    @Override
    public void savePerformanceSetting(PerformanceSetting performanceSetting) {
        Setting setting = getSetting(Key.PERFORMANCE);
        if (setting == null) {
            setting = new Setting();
            setting.setKey(Key.PERFORMANCE);
        }
        setting.setValue(performanceSetting);
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

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, ContributedAdministrationSetting> getContributedSettings() {
        Setting setting;
        if (contributedSettingsId == null) {
    		setting = getSetting(Key.CONTRIBUTED_SETTINGS);
    		Preconditions.checkNotNull(setting);
    		contributedSettingsId = setting.getId();
        } else {
            setting = load(contributedSettingsId);
        }
        return (Map<String, ContributedAdministrationSetting>) setting.getValue();
	}

	@Transactional
	@Override
	public void saveContributedSettings(Map<String, ContributedAdministrationSetting> contributedSettings) {
		Setting setting = getSetting(Key.CONTRIBUTED_SETTINGS);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.CONTRIBUTED_SETTINGS);
		}
		setting.setValue((Serializable) contributedSettings);
		dao.persist(setting);
	}
 
	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends ContributedAdministrationSetting> T getContributedSetting(Class<T> settingClass) {
		T contributedSetting = (T) getContributedSettings().get(settingClass.getName());
		if (contributedSetting == null) {
			try {
				T value = settingClass.newInstance();
				if (OneDev.getInstance(Validator.class).validate(value).isEmpty()) 
					contributedSetting = value;
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		return contributedSetting;
	}

	public void saveContributedSetting(Class<? extends ContributedAdministrationSetting> settingClass, 
			@Nullable ContributedAdministrationSetting setting) {
		Map<String, ContributedAdministrationSetting> contributedSettings = getContributedSettings();
		contributedSettings.put(settingClass.getName(), setting);
		saveContributedSettings(contributedSettings);
	}

	@Sessional
	@Override
	public ServiceDeskSetting getServiceDeskSetting() {
        Setting setting;
        if (serviceDeskSettingId == null) {
    		setting = getSetting(Key.SERVICE_DESK_SETTING);
    		Preconditions.checkNotNull(setting);
    		serviceDeskSettingId = setting.getId();
        } else {
            setting = load(serviceDeskSettingId);
        }
        return (ServiceDeskSetting) setting.getValue();
	}

	@Transactional
	@Override
	public void saveServiceDeskSetting(ServiceDeskSetting serviceDeskSetting) {
		Setting setting = getSetting(Key.SERVICE_DESK_SETTING);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.SERVICE_DESK_SETTING);
		}
		setting.setValue((Serializable) serviceDeskSetting);
		dao.persist(setting);
	}

	@Override
	public Collection<String> getUndefinedIssueFields() {
		Collection<String> undefinedFields = new HashSet<>();
		undefinedFields.addAll(getIssueSetting().getUndefinedFields());
		if (getServiceDeskSetting() != null)
			undefinedFields.addAll(getServiceDeskSetting().getUndefinedIssueFields());
		return undefinedFields;
	}

	@Override
	public Collection<UndefinedFieldValue> getUndefinedIssueFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		undefinedFieldValues.addAll(getIssueSetting().getUndefinedFieldValues());
		if (getServiceDeskSetting() != null)
			undefinedFieldValues.addAll(getServiceDeskSetting().getUndefinedIssueFieldValues());
		return undefinedFieldValues;
	}

	@Override
	public Collection<String> fixUndefinedIssueFields(Map<String, UndefinedFieldResolution> resolutions) {
		Collection<String> deletedFields = new HashSet<>();
		deletedFields.addAll(getIssueSetting().fixUndefinedFields(resolutions));
		if (getServiceDeskSetting() != null)
			getServiceDeskSetting().fixUndefinedIssueFields(resolutions);
		return deletedFields;
	}
	
	@Override
	public Collection<String> fixUndefinedIssueFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		Collection<String> deletedFields = new HashSet<>();
		deletedFields.addAll(getIssueSetting().fixUndefinedFieldValues(resolutions));
		if (getServiceDeskSetting() != null)
			getServiceDeskSetting().fixUndefinedIssueFieldValues(resolutions);
		return deletedFields;
	}

	@Override
	public void onRenameRole(String oldName, String newName) {
		getIssueSetting().onRenameRole(oldName, newName);
		if (getServiceDeskSetting() != null)
			getServiceDeskSetting().onRenameRole(oldName, newName);
	}

	@Override
	public Usage onDeleteRole(String roleName) {
		Usage usage = new Usage();
		
		usage.add(getIssueSetting().onDeleteRole(roleName));
		if (getServiceDeskSetting() != null)
			usage.add(getServiceDeskSetting().onDeleteRole(roleName));
		
		return usage.prefix("administration");
	}

	@Override
	public void onMoveProject(String oldPath, String newPath) {
    	for (JobExecutor jobExecutor: getJobExecutors())
    		jobExecutor.onMoveProject(oldPath, newPath);
    	for (GroovyScript groovyScript: getGroovyScripts())
    		groovyScript.onMoveProject(oldPath, newPath);
    	if (getServiceDeskSetting() != null)
    		getServiceDeskSetting().onMoveProject(oldPath, newPath);
    	getIssueSetting().onMoveProject(oldPath, newPath);
	}

	@Override
	public Usage onDeleteProject(String projectPath) {
    	Usage usage = new Usage();
    	int index = 1;
    	for (JobExecutor jobExecutor: getJobExecutors()) {
    		usage.add(jobExecutor.onDeleteProject(projectPath).prefix("job executor #" + index));
    		index++;
    	}
    	
    	index = 1;
    	for (GroovyScript groovyScript: getGroovyScripts()) {
    		usage.add(groovyScript.onDeleteProject(projectPath).prefix("groovy script #" + index));
    		index++;
    	}
    	if (getServiceDeskSetting() != null)
    		usage.add(getServiceDeskSetting().onDeleteProject(projectPath));
    	usage.add(getIssueSetting().onDeleteProject(projectPath));
		
		return usage.prefix("administration");
	}

	@Override
	public void onRenameGroup(String oldName, String newName) {
		Authenticator authenticator = getAuthenticator();
		if (authenticator != null) 
			authenticator.onRenameGroup(oldName, newName);
		getIssueSetting().onRenameGroup(oldName, newName);
		getSecuritySetting().onRenameGroup(oldName, newName);
	}

	@Override
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		
		usage.add(getIssueSetting().onDeleteGroup(groupName));
		usage.add(getSecuritySetting().onDeleteGroup(groupName));

		Authenticator authenticator = getAuthenticator();
		if (authenticator != null)
			usage.add(authenticator.onDeleteGroup(groupName));
		
		return usage.prefix("administration");
	}

	@Override
	public void onRenameUser(String oldName, String newName) {
    	for (JobExecutor jobExecutor: getJobExecutors())
    		jobExecutor.onRenameUser(oldName, newName);
		getIssueSetting().onRenameUser(oldName, newName);
	}

	@Override
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		
    	int index = 1;
    	for (JobExecutor jobExecutor: getJobExecutors()) {
    		usage.add(jobExecutor.onDeleteUser(userName).prefix("job executor #" + index));
    		index++;
    	}

		usage.add(getIssueSetting().onDeleteUser(userName));
		
		return usage.prefix("administration");
	}
	
	@Override
	public void onRenameLink(String oldName, String newName) {
		getIssueSetting().onRenameLink(oldName, newName);
	}

	@Override
	public Usage onDeleteLink(String linkName) {
		Usage usage = new Usage();
		usage.add(getIssueSetting().onDeleteLink(linkName));
		return usage.prefix("administration");
	}

	@Sessional
    @Override
    public GpgSetting getGpgSetting() {
        Setting setting;
        if (gpgSettingId == null) {
            setting = getSetting(Key.GPG);
            Preconditions.checkNotNull(setting);
            gpgSettingId = setting.getId();
        } else {
            setting = load(gpgSettingId);
        }
        return (GpgSetting)setting.getValue();
    }

    @Transactional
    @Override
    public void saveGpgSetting(GpgSetting gpgSetting) {
        Setting setting = getSetting(Key.GPG);
        if (setting == null) {
            setting = new Setting();
            setting.setKey(Key.GPG);
        }
        setting.setValue(gpgSetting);
        dao.persist(setting);
    }

}
