package io.onedev.server.service.impl;

import static io.onedev.server.model.Setting.PROP_KEY;
import static org.hibernate.criterion.Restrictions.eq;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.CipherService;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.support.administration.AgentSetting;
import io.onedev.server.model.support.administration.AlertSetting;
import io.onedev.server.model.support.administration.AuditSetting;
import io.onedev.server.model.support.administration.BackupSetting;
import io.onedev.server.model.support.administration.BrandingSetting;
import io.onedev.server.model.support.administration.ClusterSetting;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.GlobalPackSetting;
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
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.mailservice.MailConnector;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;

@Singleton
public class DefaultSettingService extends BaseEntityService<Setting> implements SettingService, Serializable {
	
	private static final byte[] UUID_ENCRYPTION_KEY = new byte[] {
		(byte)0x5a, (byte)0x1f, (byte)0x9c, (byte)0x7e,
		(byte)0x3b, (byte)0x44, (byte)0xd2, (byte)0x99,
		(byte)0x2f, (byte)0xa3, (byte)0x7b, (byte)0xce,
		(byte)0x1c, (byte)0x58, (byte)0xfa, (byte)0x91,
		(byte)0x8d, (byte)0x26, (byte)0x74, (byte)0xcb,
		(byte)0xe2, (byte)0x3d, (byte)0xa4, (byte)0x10,
		(byte)0x3f, (byte)0xbf, (byte)0x58, (byte)0x60,
		(byte)0x4b, (byte)0x8c, (byte)0xad, (byte)0xee
	};
		
	private static final CipherService cipherService = new AesCipherService();

	private final Map<Key, Optional<Serializable>> cache = new ConcurrentHashMap<>();

	@Inject
	private ClusterService clusterService;

	@Sessional
	@Listen
	public void on(SystemStarting event) {
		for (var setting: query()) {
			cache.put(setting.getKey(), Optional.ofNullable(setting.getValue()));
		}
	}
	
	@Sessional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Setting) {
			Setting setting = (Setting) event.getEntity();
			var settingValue = setting.getValue();
			clusterService.runOnAllServers(() -> {
				cache.put(setting.getKey(), Optional.ofNullable(settingValue));
				return null;
			});
		}
	}
	
	@Sessional
	@Override
	public Setting findSetting(Key key) {
		return find(newCriteria().add(eq(PROP_KEY, key)));
	}
	
	@Nullable
	private Serializable getSettingValue(Key key) {
		var settingValue = cache.get(key);	
		return settingValue != null? settingValue.orElse(null): null;
	}
	
	@Override
	public SystemSetting getSystemSetting() {
		return (SystemSetting) getSettingValue(Key.SYSTEM);
	}

	@Override
	public AlertSetting getAlertSetting() {
		return (AlertSetting) getSettingValue(Key.ALERT);
	}
	
	@Override
	public String getSystemUUID() {
		return new String(cipherService.decrypt(Base64.decodeBase64(((String)getSettingValue(Key.SYSTEM_UUID)).getBytes()), UUID_ENCRYPTION_KEY).getBytes());
	}

	@Override
	public String getSubscriptionData() {
		return (String) getSettingValue(Key.SUBSCRIPTION_DATA);
	}
	
	@Transactional
	protected void saveSetting(Key key, Serializable value) {
		var setting = findSetting(key);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(key);
		}
		setting.setValue(value);
		dao.persist(setting);
	}

	@Override
	public BackupSetting getBackupSetting() {
		return (BackupSetting) getSettingValue(Key.BACKUP);
	}

	@Override
	public BrandingSetting getBrandingSetting() {
        return (BrandingSetting) getSettingValue(Key.BRANDING);
	}
	
	@Override
	public ClusterSetting getClusterSetting() {
		return (ClusterSetting) getSettingValue(Key.CLUSTER_SETTING);
	}

	@Override
	public AuditSetting getAuditSetting() {
		return (AuditSetting) getSettingValue(Key.AUDIT);
	}

	@Override
	public SecuritySetting getSecuritySetting() {
		return (SecuritySetting) getSettingValue(Key.SECURITY);
	}

	@Override
	public GlobalIssueSetting getIssueSetting() {
		return (GlobalIssueSetting) getSettingValue(Key.ISSUE);
	}

	@Override
	public Authenticator getAuthenticator() {
		return (Authenticator) getSettingValue(Key.AUTHENTICATOR);
	}
	
	@Override
	public MailConnector getMailConnector() {
		return (MailConnector) getSettingValue(Key.MAIL);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JobExecutor> getJobExecutors() {
		return (List<JobExecutor>) getSettingValue(Key.JOB_EXECUTORS);
	}

	@Override
	public EmailTemplates getEmailTemplates() {
		return (EmailTemplates) getSettingValue(Key.EMAIL_TEMPLATES);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GroovyScript> getGroovyScripts() {
		return (List<GroovyScript>) getSettingValue(Key.GROOVY_SCRIPTS);
	}

	@Override
	public GlobalPullRequestSetting getPullRequestSetting() {
		return (GlobalPullRequestSetting) getSettingValue(Key.PULL_REQUEST);
	}

	@Override
	public GlobalBuildSetting getBuildSetting() {
		return (GlobalBuildSetting) getSettingValue(Key.BUILD);
	}

	@Override
	public GlobalPackSetting getPackSetting() {
		return (GlobalPackSetting) getSettingValue(Key.PACK);
	}
	
	@Override
	public GlobalProjectSetting getProjectSetting() {
		return (GlobalProjectSetting) getSettingValue(Key.PROJECT);
	}

	@Override
	public AgentSetting getAgentSetting() {
		return (AgentSetting) getSettingValue(Key.AGENT);
	}

    @Override
    public SshSetting getSshSetting() {
    	return (SshSetting) getSettingValue(Key.SSH);
    }

    @Override
    public PerformanceSetting getPerformanceSetting() {
    	return (PerformanceSetting) getSettingValue(Key.PERFORMANCE);
    }

	@Override
	public ServiceDeskSetting getServiceDeskSetting() {
		return (ServiceDeskSetting) getSettingValue(Key.SERVICE_DESK_SETTING); 
	}

    @Override
    public GpgSetting getGpgSetting() {
    	return (GpgSetting) getSettingValue(Key.GPG);
    }
	
	@Transactional
	@Override
	public void saveSystemSetting(SystemSetting systemSetting) {
		saveSetting(Key.SYSTEM, systemSetting);
	}

	@Transactional
	@Override
	public void saveAlertSetting(AlertSetting alertSetting) {
		saveSetting(Key.ALERT, alertSetting);
	}

	@Transactional
	@Override
	public void saveSystemUUID(String systemUUID) {
		saveSetting(Key.SYSTEM_UUID, encryptUUID(systemUUID));
	}
	
	@Transactional
	@Override
	public void saveSubscriptionData(String subscriptionData) {
		saveSetting(Key.SUBSCRIPTION_DATA, subscriptionData);
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
	public void saveClusterSetting(ClusterSetting clusterSetting) {
		saveSetting(Key.CLUSTER_SETTING, clusterSetting);
	}

	@Transactional
	@Override
	public void saveAuditSetting(AuditSetting auditSetting) {
		saveSetting(Key.AUDIT, auditSetting);
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
	public void saveMailConnector(MailConnector mailConnector) {
		saveSetting(Key.MAIL, mailConnector);
	}

	@Transactional
	@Override
	public void saveJobExecutors(List<JobExecutor> jobExecutors) {
		saveSetting(Key.JOB_EXECUTORS, (Serializable) jobExecutors);
	}

	@Transactional
	@Override
	public void saveEmailTemplates(EmailTemplates emailTemplates) {
		saveSetting(Key.EMAIL_TEMPLATES, emailTemplates);
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
	public void savePackSetting(GlobalPackSetting packSetting) {
		saveSetting(Key.PACK, packSetting);
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
	public void saveContributedSettings(Map<String, ContributedAdministrationSetting> contributedSettings) {
		saveSetting(Key.CONTRIBUTED_SETTINGS, (Serializable) contributedSettings);
	}
    
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, ContributedAdministrationSetting> getContributedSettings() {
		return (Map<String, ContributedAdministrationSetting>) getSettingValue(Key.CONTRIBUTED_SETTINGS);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends ContributedAdministrationSetting> T getContributedSetting(Class<T> settingClass) {
		T setting = (T) getContributedSettings().get(settingClass.getName());
		if (setting == null) {
			try {
				T value = settingClass.getDeclaredConstructor().newInstance();
				if (OneDev.getInstance(Validator.class).validate(value).isEmpty()) 
					setting = value;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		return setting;
	}

	@Override
	public void saveContributedSetting(ContributedAdministrationSetting setting) {
		var contributedSettings = getContributedSettings();
		contributedSettings.put(setting.getClass().getName(), setting);
		saveSetting(Key.CONTRIBUTED_SETTINGS, (Serializable) contributedSettings);
	}

	@Override
	public void removeContributedSetting(Class<? extends ContributedAdministrationSetting> settingClass) {
		var contributedSettings = getContributedSettings();
		contributedSettings.remove(settingClass.getName());
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
		saveSetting(Key.ISSUE, getIssueSetting());
		saveSetting(Key.SERVICE_DESK_SETTING, getServiceDeskSetting());
	}

	@Override
	public Usage onDeleteRole(String roleName) {
		Usage usage = new Usage();
		usage.add(getIssueSetting().onDeleteRole(roleName));
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
		
    	for (JobExecutor jobExecutor: getJobExecutors()) 
    		usage.add(jobExecutor.onDeleteProject(projectPath).prefix("job executor '" + jobExecutor.getName() + "'"));
    	for (GroovyScript groovyScript: getGroovyScripts()) 
    		usage.add(groovyScript.onDeleteProject(projectPath).prefix("groovy script '" + groovyScript.getName() + "'"));
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
		getIssueSetting().onRenameUser(oldName, newName);
		getAlertSetting().onRenameUser(oldName, newName);
		
		saveSetting(Key.JOB_EXECUTORS, (Serializable) getJobExecutors());
		saveSetting(Key.ISSUE, getIssueSetting());
	}

	@Override
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
				
		usage.add(getIssueSetting().onDeleteUser(userName));
		usage.add(getAlertSetting().onDeleteUser(userName));
		
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

	@Override
	public String encryptUUID(String uuid) {
		return new String(Base64.encodeBase64(cipherService.encrypt(uuid.getBytes(), UUID_ENCRYPTION_KEY).getBytes()));
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(SettingService.class);
	}
	
}
