package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.Config;
import com.pmease.gitplex.core.setting.MailSetting;
import com.pmease.gitplex.core.setting.SystemSetting;

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
	MailSetting getMailSetting();

	/**
	 * Save specified mail setting.
	 * <p>
	 * @param mailSetting
	 * 			mail setting to be saved. Use <tt>null</tt> to clear the setting (but 
	 * 			setting record will still be remained in database)
	 */
	void saveMailSetting(@Nullable MailSetting mailSetting);
	
}
