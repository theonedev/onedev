package com.gitplex.server.manager;

import javax.annotation.Nullable;

import com.gitplex.server.model.Config;
import com.gitplex.server.model.support.setting.BackupSetting;
import com.gitplex.server.model.support.setting.MailSetting;
import com.gitplex.server.model.support.setting.SecuritySetting;
import com.gitplex.server.model.support.setting.SystemSetting;
import com.gitplex.server.persistence.dao.EntityManager;
import com.gitplex.server.security.authenticator.Authenticator;

public interface ConfigManager extends EntityManager<Config> {
	
	/**
	 * Retrieve config by key.
	 * <p>
	 * @param key
	 *			key of the config
	 * @return
	 * 			config associated with specified key, or <tt>null</tt> if 
	 * 			no config record found for the key
	 */
	Config getConfig(Config.Key key);
	
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
	
	@Nullable
	Authenticator getAuthenticator();
	
	void saveAuthenticator(@Nullable Authenticator authenticator);
	
}
