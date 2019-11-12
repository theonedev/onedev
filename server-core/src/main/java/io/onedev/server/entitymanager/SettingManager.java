package io.onedev.server.entitymanager;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Setting;
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
import io.onedev.server.persistence.dao.EntityManager;

public interface SettingManager extends EntityManager<Setting> {
	
	/**
	 * Retrieve setting by key.
	 * <p>
	 * @param key
	 *			key of the setting
	 * @return
	 * 			setting associated with specified key, or <tt>null</tt> if 
	 * 			no setting record found for the key
	 */
	Setting getSetting(Setting.Key key);
	
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

	SecuritySetting getSecuritySetting();
	
	void saveSecuritySetting(SecuritySetting securitySetting);
	
	IssueSetting getIssueSetting();
	
	void saveIssueSetting(IssueSetting issueSetting);
	
	@Nullable
	Authenticator getAuthenticator();
	
	void saveAuthenticator(@Nullable Authenticator authenticator);

	List<JobExecutor> getJobExecutors();
	
	void saveJobExecutors(List<JobExecutor> jobExecutors);

	List<GroovyScript> getGroovyScripts();
	
	void saveGroovyScripts(List<GroovyScript> jobScripts);
	
	PullRequestSetting getPullRequestSetting();
	
	void savePullRequestSetting(PullRequestSetting pullRequestSetting);
	
	BuildSetting getBuildSetting();
	
	void saveBuildSetting(BuildSetting buildSetting);
	
}
