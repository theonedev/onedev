package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Setting;
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
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;

public interface SettingManager extends EntityManager<Setting> {
	
	void init();
	
	/**
	 * Retrieve setting by key.
	 * <p>
	 * @param key
	 *			key of the setting
	 * @return
	 * 			setting associated with specified key, or <tt>null</tt> if 
	 * 			no setting record found for the key
	 */
	@Nullable
	Setting findSetting(Setting.Key key);
	
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
	SystemSetting getSystemSetting();
	
	/**
	 * Save specified system setting.
	 * <p>
	 * @param systemSetting
	 * 			system setting to be saved
	 */
	void saveSystemSetting(SystemSetting systemSetting);
	
	/**
	 * Get mail setting.
	 * <p>
	 * @return
	 * 			mail setting, or <tt>null</tt> if mail setting record exists but value is 
	 * 			null.
	 * @throws 
	 * 			RuntimeException if mail setting record is not found
	 */
	@Nullable
	MailSetting getMailSetting();

	/**
	 * Save specified mail setting.
	 * <p>
	 * @param mailSetting
	 * 			mail setting to be saved. Use <tt>null</tt> to clear the setting (but 
	 * 			setting record will still be remained in database)
	 */
	void saveMailSetting(@Nullable MailSetting mailSetting);
	
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
	
	@Nullable
	BrandingSetting getBrandingSetting();
	
	void saveBrandingSetting(@Nullable BrandingSetting brandingSetting);

	SecuritySetting getSecuritySetting();
	
	void saveSecuritySetting(SecuritySetting securitySetting);
	
	GlobalIssueSetting getIssueSetting();
	
	void saveIssueSetting(GlobalIssueSetting issueSetting);
	
	@Nullable
	Authenticator getAuthenticator();
	
	void saveAuthenticator(@Nullable Authenticator authenticator);

	List<JobExecutor> getJobExecutors();
	
	void saveJobExecutors(List<JobExecutor> jobExecutors);

	NotificationTemplateSetting getNotificationTemplateSetting();
	
	void saveNotificationTemplateSetting(NotificationTemplateSetting notificationTemplateSetting);
	
	@Nullable
	ServiceDeskSetting getServiceDeskSetting();
	
	void saveServiceDeskSetting(ServiceDeskSetting serviceDeskSetting);
	
	List<GroovyScript> getGroovyScripts();
	
	void saveGroovyScripts(List<GroovyScript> jobScripts);
	
	GlobalPullRequestSetting getPullRequestSetting();
	
	void savePullRequestSetting(GlobalPullRequestSetting pullRequestSetting);
	
	GlobalBuildSetting getBuildSetting();
	
	void saveBuildSetting(GlobalBuildSetting buildSetting);
	
	GlobalProjectSetting getProjectSetting();
	
	void saveProjectSetting(GlobalProjectSetting projectSetting);

	AgentSetting getAgentSetting();
	
	void saveAgentSetting(AgentSetting agentSetting);
	
    SshSetting getSshSetting();
    
    GpgSetting getGpgSetting();
    
    void savePerformanceSetting(PerformanceSetting performanceSetting);

    PerformanceSetting getPerformanceSetting();

    void saveSshSetting(SshSetting sshSetting);
    
    void saveGpgSetting(GpgSetting gpgSetting);

    List<SsoConnector> getSsoConnectors();
    
    void saveSsoConnectors(List<SsoConnector> ssoConnectors);
    
    Map<String, ContributedAdministrationSetting> getContributedSettings();
    
    void saveContributedSettings(Map<String, ContributedAdministrationSetting> contributedSettings);

    @Nullable
    <T extends ContributedAdministrationSetting> T getContributedSetting(Class<T> settingClass);
    
    void saveContributedSetting(Class<? extends ContributedAdministrationSetting> settingClass, 
    		@Nullable ContributedAdministrationSetting setting);
    
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
	
}
