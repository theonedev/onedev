package com.pmease.gitop.core.manager;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultConfigManager;
import com.pmease.gitop.core.model.Config;
import com.pmease.gitop.core.setting.MailSetting;
import com.pmease.gitop.core.setting.StorageSetting;

@ImplementedBy(DefaultConfigManager.class)
public interface ConfigManager extends GenericDao<Config> {
	
	/**
	 * Retrieve config by key.
	 * <p>
	 * @param key
	 *			key of the config
	 * @return
	 * 			config associated with specified key, or <i>null</i> if 
	 * 			no config record found for the key
	 */
	Config getConfig(Config.Key key);
	
	/**
	 * Get storage setting.
	 * <p>
	 * @return
	 *			storage setting, never <i>null</i>
	 * @throws
	 * 			RuntimeException if storage setting record is not found
	 * @throws
	 * 			NullPointerException if storage setting record exists but value is null
	 */
	StorageSetting getStorageSetting();
	
	/**
	 * Save specified storage setting.
	 * <p>
	 * @param storageSetting
	 * 			storage setting to be saved
	 */
	void saveStorageSetting(StorageSetting storageSetting);
	
	/**
	 * Get mail setting.
	 * <p>
	 * @return
	 * 			mail setting, or <i>null</i> if mail setting record exists but value is 
	 * 			null.
	 * @throws 
	 * 			RuntimeException if mail setting record is not found
	 */
	MailSetting getMailSetting();

	/**
	 * Save specified mail setting.
	 * <p>
	 * @param mailSetting
	 * 			mail setting to be saved. Use <i>null</i> to clear the setting (but 
	 * 			setting record will still be remained in database)
	 */
	void saveMailSetting(@Nullable MailSetting mailSetting);
}
