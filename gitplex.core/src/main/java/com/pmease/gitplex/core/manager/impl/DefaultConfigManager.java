package com.pmease.gitplex.core.manager.impl;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Config;
import com.pmease.gitplex.core.entity.Config.Key;
import com.pmease.gitplex.core.extensionpoint.ConfigListener;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.setting.MailSetting;
import com.pmease.gitplex.core.setting.SystemSetting;

@Singleton
public class DefaultConfigManager extends AbstractEntityDao<Config> implements ConfigManager {
	
	private final Provider<Set<ConfigListener>> listenersProvider;
	
	private volatile Long systemSettingConfigId;
	
	private volatile Long mailSettingConfigId;
	
	@Inject
	public DefaultConfigManager(Dao dao, Provider<Set<ConfigListener>> listenersProvider) {
		super(dao);
		
		this.listenersProvider = listenersProvider;
	}

	@Sessional
	@Override
	public SystemSetting getSystemSetting() {
        Config config;
        if (systemSettingConfigId == null) {
    		config = getConfig(Key.SYSTEM);
    		Preconditions.checkNotNull(config);
            systemSettingConfigId = config.getId();
        } else {
            config = load(systemSettingConfigId);
        }
        SystemSetting setting = (SystemSetting) config.getSetting();
        Preconditions.checkNotNull(setting);
        return setting;
	}

	@Transactional
	@Override
	public void saveSystemSetting(SystemSetting systemSetting) {
		Preconditions.checkNotNull(systemSetting);
		
		Config config = getConfig(Key.SYSTEM);
		if (config == null) {
			config = new Config();
			config.setKey(Key.SYSTEM);
		}
		config.setSetting(systemSetting);
		persist(config);
		
		for (ConfigListener listener: listenersProvider.get())
			listener.onSave(config);
	}

	@Sessional
	@Override
	public Config getConfig(Key key) {
		return find(EntityCriteria.of(Config.class).add(Restrictions.eq("key", key)));
	}

	@Sessional
	@Override
	public MailSetting getMailSetting() {
        Config config;
        if (mailSettingConfigId == null) {
    		config = getConfig(Key.MAIL);
    		Preconditions.checkNotNull(config);
    		mailSettingConfigId = config.getId();
        } else {
            config = load(mailSettingConfigId);
        }
        return (MailSetting) config.getSetting();
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
		persist(config);

		for (ConfigListener listener: listenersProvider.get())
			listener.onSave(config);
	}

}
