package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.core.model.Config;
import com.pmease.gitop.core.model.Config.Key;
import com.pmease.gitop.core.setting.MailSetting;
import com.pmease.gitop.core.setting.StorageSetting;

@Singleton
public class DefaultConfigManager extends AbstractGenericDao<Config> implements ConfigManager {

	@Inject
	public DefaultConfigManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Sessional
	@Override
	public StorageSetting getStorageSetting() {
		Config config = getConfig(Key.STORAGE);
		if (config != null) {
			StorageSetting storageSetting = (StorageSetting) config.getSetting();
			Preconditions.checkNotNull(storageSetting);
			return storageSetting;
		} else {
			throw new RuntimeException("Unable to find storage setting record.");
		}
	}

	@Transactional
	@Override
	public void saveStorageSetting(StorageSetting storageSetting) {
		Preconditions.checkNotNull(storageSetting);
		
		Config config = getConfig(Key.STORAGE);
		if (config == null) {
			config = new Config();
			config.setKey(Key.STORAGE);
		}
		config.setSetting(storageSetting);
		save(config);
	}

	@Sessional
	@Override
	public Config getConfig(Key key) {
		return find(new Criterion[]{Restrictions.eq("key", key)});
	}

	@Sessional
	@Override
	public MailSetting getMailSetting() {
		Config config = getConfig(Key.MAIL);
		if (config != null) {
			MailSetting mailSetting = (MailSetting) config.getSetting();
			return mailSetting;
		} else {
			throw new RuntimeException("Unable to find mail setting record.");
		}
	}

	@Transactional
	@Override
	public void saveMailSetting(MailSetting mailSetting) {
		Config config = getConfig(Key.MAIL);
		if (config == null) {
			config = new Config();
			config.setKey(Key.MAIL);
		}
		config.setSetting(mailSetting);
		save(config);
	}

}
