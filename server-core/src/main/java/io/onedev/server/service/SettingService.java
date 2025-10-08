package io.onedev.server.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.onedev.server.annotation.NoDBAccess;
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
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;

public interface SettingService extends EntityService<Setting> {
	
	@Nullable
	Setting findSetting(Key key);
	
	/**
	 * Get system setting.
	 * <p>
	 * @return
	 *			system setting, never <tt>null</tt>
	 * @throws
	 * 			RuntimeException if system setting record is not found
	 * @throws
	 * 			NullPointerException if system setting record exists but value is null
	 */
	@NoDBAccess
	SystemSetting getSystemSetting();

	AlertSetting getAlertSetting();
	
	String getSystemUUID();

	@NoDBAccess
	@Nullable
	String getSubscriptionData();
	
	/**
	 * Save specified system setting.
	 * <p>
	 * @param systemSetting
	 * 			system setting to be saved
	 */
	void saveSystemSetting(SystemSetting systemSetting);
	
	void saveAlertSetting(AlertSetting alertSetting);
	
	void saveSubscriptionData(@Nullable String subscriptionData);
	
	/**
	 * Get backup setting.
	 * <p>
	 * @return
	 * 			backup setting, or <tt>null</tt> if backup setting record exists but value is null
	 * @throws 
	 * 			RuntimeException if backup setting record is not found
	 */
	@Nullable
	BackupSetting getBackupSetting();

	/**
	 * Save specified backup setting.
	 * <p>
	 * @param backupSetting
	 * 			backup setting to be saved. Use <tt>null</tt> to clear the setting (but 
	 * 			setting record will still be remained in database)
	 */
	void saveBackupSetting(@Nullable BackupSetting backupSetting);
	
	BrandingSetting getBrandingSetting();
	
	void saveBrandingSetting(BrandingSetting brandingSetting);
	
	ClusterSetting getClusterSetting();

	void saveClusterSetting(ClusterSetting clusterSetting);

	AuditSetting getAuditSetting();

	void saveAuditSetting(AuditSetting auditSetting);
	
	SecuritySetting getSecuritySetting();
	
	void saveSecuritySetting(SecuritySetting securitySetting);
	
	GlobalIssueSetting getIssueSetting();
	
	void saveIssueSetting(GlobalIssueSetting issueSetting);
	
	@Nullable
	Authenticator getAuthenticator();
	
	void saveAuthenticator(@Nullable Authenticator authenticator);

	@Nullable
    MailConnector getMailConnector();
	
	void saveMailConnector(@Nullable MailConnector mailConnector);

	List<JobExecutor> getJobExecutors();
	
	void saveJobExecutors(List<JobExecutor> jobExecutors);

	EmailTemplates getEmailTemplates();
	
	void saveEmailTemplates(EmailTemplates emailTemplates);
	
	@Nullable
	ServiceDeskSetting getServiceDeskSetting();
	
	void saveServiceDeskSetting(ServiceDeskSetting serviceDeskSetting);
	
	List<GroovyScript> getGroovyScripts();
	
	void saveGroovyScripts(List<GroovyScript> jobScripts);
	
	GlobalPullRequestSetting getPullRequestSetting();
	
	void savePullRequestSetting(GlobalPullRequestSetting pullRequestSetting);
	
	GlobalBuildSetting getBuildSetting();
	
	void saveBuildSetting(GlobalBuildSetting buildSetting);
	
	GlobalPackSetting getPackSetting();

	void savePackSetting(GlobalPackSetting packSetting);
	
	GlobalProjectSetting getProjectSetting();
	
	void saveProjectSetting(GlobalProjectSetting projectSetting);

	AgentSetting getAgentSetting();
	
	void saveAgentSetting(AgentSetting agentSetting);
	
	void saveSystemUUID(String systemUUID);
	
    SshSetting getSshSetting();
    
    GpgSetting getGpgSetting();
    
    void savePerformanceSetting(PerformanceSetting performanceSetting);

    PerformanceSetting getPerformanceSetting();

    void saveSshSetting(SshSetting sshSetting);
    
    void saveGpgSetting(GpgSetting gpgSetting);
    
    Map<String, ContributedAdministrationSetting> getContributedSettings();
    
    void saveContributedSettings(Map<String, ContributedAdministrationSetting> contributedSettings);

	@NoDBAccess
    @Nullable
    <T extends ContributedAdministrationSetting> T getContributedSetting(Class<T> settingClass);
    
    void saveContributedSetting(ContributedAdministrationSetting setting);

	void removeContributedSetting(Class<? extends ContributedAdministrationSetting> settingClass);
    
	Collection<String> getUndefinedIssueFields();

	Collection<UndefinedFieldValue> getUndefinedIssueFieldValues();
	
	Collection<String> fixUndefinedIssueFields(Map<String, UndefinedFieldResolution> resolutions);
	
	Collection<String> fixUndefinedIssueFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions);

	void onRenameRole(String oldName, String newName);
	
	Usage onDeleteRole(String roleName);

	void onMoveProject(String oldPath, String newPath);
	
	Usage onDeleteProject(String projectPath);

	void onRenameGroup(String oldName, String newName);

	Usage onDeleteGroup(String groupName);

	void onRenameUser(String oldName, String newName);

	Usage onDeleteUser(String userName);

	void onRenameLink(String oldName, String name);

	Usage onDeleteLink(String linkName);
	
	String encryptUUID(String uuid);

}
