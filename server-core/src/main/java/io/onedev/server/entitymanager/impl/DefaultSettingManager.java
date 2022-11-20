package io.onedev.server.entitymanager.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.SettingManager;
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
import io.onedev.server.persistence.DataManager;
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
	
	private final ClusterManager clusterManager;
	
	private volatile Map<Key, Serializable> settingValues;
	
	@Inject
	public DefaultSettingManager(Dao dao, DataManager dataManager, ClusterManager clusterManager) {
		super(dao);
		this.clusterManager = clusterManager;
	}
	
	@Override
	@Sessional
	public void init() {
		settingValues = clusterManager.getHazelcastInstance().getReplicatedMap("settingValues");
		for (Setting setting: query()) {
			if (setting.getValue() != null)
				settingValues.put(setting.getKey(), setting.getValue());
		}
	}
	
	@Override
	public SystemSetting getSystemSetting() {
		return (SystemSetting) settingValues.get(Key.SYSTEM);
	}

	@Sessional
	@Override
	public Setting findSetting(Key key) {
		return find(EntityCriteria.of(Setting.class).add(Restrictions.eq("key", key)));
	}

	private void saveSetting(Key key, Serializable value) {
		Setting setting = findSetting(key);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(key);
		}
		setting.setValue(value);
		dao.persist(setting);
		
		if (value != null)
			settingValues.put(key, value);
		else
			settingValues.remove(key);
	}
	
	@Override
	public MailSetting getMailSetting() {
		return (MailSetting) settingValues.get(Key.MAIL);
	}

	@Override
	public BackupSetting getBackupSetting() {
		return (BackupSetting) settingValues.get(Key.BACKUP);
	}

	@Override
	public BrandingSetting getBrandingSetting() {
        return (BrandingSetting) settingValues.get(Key.BRANDING);
	}

	@Override
	public SecuritySetting getSecuritySetting() {
		return (SecuritySetting) settingValues.get(Key.SECURITY);
	}

	@Override
	public GlobalIssueSetting getIssueSetting() {
		return (GlobalIssueSetting) settingValues.get(Key.ISSUE);
	}

	@Override
	public Authenticator getAuthenticator() {
		return (Authenticator) settingValues.get(Key.AUTHENTICATOR);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JobExecutor> getJobExecutors() {
		return (List<JobExecutor>) settingValues.get(Key.JOB_EXECUTORS);
	}

	@Override
	public NotificationTemplateSetting getNotificationTemplateSetting() {
		return (NotificationTemplateSetting) settingValues.get(Key.NOTIFICATION_TEMPLATE_SETTING);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GroovyScript> getGroovyScripts() {
		return (List<GroovyScript>) settingValues.get(Key.GROOVY_SCRIPTS);
	}

	@Override
	public GlobalPullRequestSetting getPullRequestSetting() {
		return (GlobalPullRequestSetting) settingValues.get(Key.PULL_REQUEST);
	}

	@Override
	public GlobalBuildSetting getBuildSetting() {
		return (GlobalBuildSetting) settingValues.get(Key.BUILD);
	}

	@Override
	public GlobalProjectSetting getProjectSetting() {
		return (GlobalProjectSetting) settingValues.get(Key.PROJECT);
	}

	@Override
	public AgentSetting getAgentSetting() {
		return (AgentSetting) settingValues.get(Key.AGENT);
	}

    @Override
    public SshSetting getSshSetting() {
    	return (SshSetting) settingValues.get(Key.SSH);
    }

    @Override
    public PerformanceSetting getPerformanceSetting() {
    	return (PerformanceSetting) settingValues.get(Key.PERFORMANCE);
    }

	@SuppressWarnings("unchecked")
	@Override
	public List<SsoConnector> getSsoConnectors() {
		return (List<SsoConnector>) settingValues.get(Key.SSO_CONNECTORS);
	}

	@Override
	public ServiceDeskSetting getServiceDeskSetting() {
		return (ServiceDeskSetting) settingValues.get(Key.SERVICE_DESK_SETTING); 
	}

    @Override
    public GpgSetting getGpgSetting() {
    	return (GpgSetting) settingValues.get(Key.GPG);
    }

	@Transactional
	@Override
	public void saveSystemSetting(SystemSetting systemSetting) {
		saveSetting(Key.SYSTEM, systemSetting);
	}
	
	@Transactional
	@Override
	public void saveMailSetting(MailSetting mailSetting) {
		saveSetting(Key.MAIL, mailSetting);
	}

	@Transactional
	@Override
	public void saveBackupSetting(BackupSetting backupSetting) {
		saveSetting(Key.BACKUP, backupSetting);
	}

	@Transactional
	@Override
	public void saveBrandingSetting(BrandingSetting brandingSetting) {
		saveSetting(Key.BRANDING, brandingSetting);
	}

	@Transactional
	@Override
	public void saveSecuritySetting(SecuritySetting securitySetting) {
		saveSetting(Key.SECURITY, securitySetting);
	}

	@Transactional
	@Override
	public void saveIssueSetting(GlobalIssueSetting issueSetting) {
		saveSetting(Key.ISSUE, issueSetting);
	}

	@Transactional
	@Override
	public void saveAuthenticator(Authenticator authenticator) {
		saveSetting(Key.AUTHENTICATOR, authenticator);
	}

	@Transactional
	@Override
	public void saveJobExecutors(List<JobExecutor> jobExecutors) {
		saveSetting(Key.JOB_EXECUTORS, (Serializable) jobExecutors);
	}

	@Transactional
	@Override
	public void saveNotificationTemplateSetting(NotificationTemplateSetting notificationTemplateSetting) {
		saveSetting(Key.NOTIFICATION_TEMPLATE_SETTING, notificationTemplateSetting);
	}

	@Transactional
	@Override
	public void saveServiceDeskSetting(ServiceDeskSetting serviceDeskSetting) {
		saveSetting(Key.SERVICE_DESK_SETTING, serviceDeskSetting);
	}

	@Transactional
	@Override
	public void saveGroovyScripts(List<GroovyScript> groovyScripts) {
		saveSetting(Key.GROOVY_SCRIPTS, (Serializable) groovyScripts);
	}

	@Transactional
	@Override
	public void savePullRequestSetting(GlobalPullRequestSetting pullRequestSetting) {
		saveSetting(Key.PULL_REQUEST, pullRequestSetting);
	}

	@Transactional
	@Override
	public void saveBuildSetting(GlobalBuildSetting buildSetting) {
		saveSetting(Key.BUILD, buildSetting);
	}

	@Transactional
	@Override
	public void saveProjectSetting(GlobalProjectSetting projectSetting) {
		saveSetting(Key.PROJECT, projectSetting);
	}

	@Transactional
	@Override
	public void saveAgentSetting(AgentSetting agentSetting) {
		saveSetting(Key.AGENT, agentSetting);
	}

	@Transactional
	@Override
	public void savePerformanceSetting(PerformanceSetting performanceSetting) {
		saveSetting(Key.PERFORMANCE, performanceSetting);
	}

	@Transactional
	@Override
	public void saveSshSetting(SshSetting sshSetting) {
		saveSetting(Key.SSH, sshSetting);
	}

	@Transactional
	@Override
	public void saveGpgSetting(GpgSetting gpgSetting) {
		saveSetting(Key.GPG, gpgSetting);
	}

	@Transactional
	@Override
	public void saveSsoConnectors(List<SsoConnector> ssoConnectors) {
		saveSetting(Key.SSO_CONNECTORS, (Serializable) ssoConnectors);
	}

	@Transactional
	@Override
	public void saveContributedSettings(Map<String, ContributedAdministrationSetting> contributedSettings) {
		saveSetting(Key.CONTRIBUTED_SETTINGS, (Serializable) contributedSettings);
	}
    
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, ContributedAdministrationSetting> getContributedSettings() {
		return (Map<String, ContributedAdministrationSetting>) settingValues.get(Key.CONTRIBUTED_SETTINGS);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends ContributedAdministrationSetting> T getContributedSetting(Class<T> settingClass) {
		T contributedSetting = (T) getContributedSettings().get(settingClass.getName());
		if (contributedSetting == null) {
			try {
				T value = settingClass.getDeclaredConstructor().newInstance();
				if (OneDev.getInstance(Validator.class).validate(value).isEmpty()) 
					contributedSetting = value;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		
		return contributedSetting;
	}

	public void saveContributedSetting(Class<? extends ContributedAdministrationSetting> settingClass, 
			@Nullable ContributedAdministrationSetting setting) {
		Map<String, ContributedAdministrationSetting> contributedSettings = getContributedSettings();
		contributedSettings.put(settingClass.getName(), setting);
		saveSetting(Key.CONTRIBUTED_SETTINGS, (Serializable) contributedSettings);
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
		saveSetting(Key.ISSUE, getIssueSetting());
		saveSetting(Key.SERVICE_DESK_SETTING, getServiceDeskSetting());
		return deletedFields;
	}
	
	@Override
	public Collection<String> fixUndefinedIssueFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		Collection<String> deletedFields = new HashSet<>();
		deletedFields.addAll(getIssueSetting().fixUndefinedFieldValues(resolutions));
		if (getServiceDeskSetting() != null)
			getServiceDeskSetting().fixUndefinedIssueFieldValues(resolutions);
		saveSetting(Key.ISSUE, getIssueSetting());
		saveSetting(Key.SERVICE_DESK_SETTING, getServiceDeskSetting());
		return deletedFields;
	}

	@Override
	public void onRenameRole(String oldName, String newName) {
		getIssueSetting().onRenameRole(oldName, newName);
		if (getServiceDeskSetting() != null)
			getServiceDeskSetting().onRenameRole(oldName, newName);
		saveSetting(Key.ISSUE, getIssueSetting());
		saveSetting(Key.SERVICE_DESK_SETTING, getServiceDeskSetting());
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
    	
		saveSetting(Key.JOB_EXECUTORS, (Serializable) getJobExecutors());
		saveSetting(Key.GROOVY_SCRIPTS, (Serializable) getGroovyScripts());
		saveSetting(Key.SERVICE_DESK_SETTING, getServiceDeskSetting());
		saveSetting(Key.ISSUE, getIssueSetting());
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
		
		saveSetting(Key.AUTHENTICATOR, getAuthenticator());
		saveSetting(Key.ISSUE, getIssueSetting());
		saveSetting(Key.SECURITY, getSecuritySetting());
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
		
		saveSetting(Key.JOB_EXECUTORS, (Serializable) getJobExecutors());
		saveSetting(Key.ISSUE, getIssueSetting());
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
		saveSetting(Key.ISSUE, getIssueSetting());
	}

	@Override
	public Usage onDeleteLink(String linkName) {
		Usage usage = new Usage();
		usage.add(getIssueSetting().onDeleteLink(linkName));
		return usage.prefix("administration");
	}

}
