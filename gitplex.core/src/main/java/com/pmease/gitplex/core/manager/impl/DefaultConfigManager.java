package com.pmease.gitplex.core.manager.impl;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.extensionpoint.ConfigListener;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.model.Config;
import com.pmease.gitplex.core.model.Config.Key;
import com.pmease.gitplex.core.setting.MailSetting;
import com.pmease.gitplex.core.setting.QosSetting;
import com.pmease.gitplex.core.setting.SystemSetting;

@Singleton
public class DefaultConfigManager implements ConfigManager {
	
	private final Dao dao;
	
	private final Provider<Set<ConfigListener>> listenersProvider;
	
	private volatile Long systemSettingConfigId;
	
	private volatile Long mailSettingConfigId;
	
	private volatile Long qosSettingConfigId;
	
	@Inject
	public DefaultConfigManager(Dao dao, Provider<Set<ConfigListener>> listenersProvider) {
		this.dao = dao;
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
            config = dao.load(Config.class, systemSettingConfigId);
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
		dao.persist(config);
		
		for (ConfigListener listener: listenersProvider.get())
			listener.onSave(config);
	}

	@Sessional
	@Override
	public Config getConfig(Key key) {
		return dao.find(EntityCriteria.of(Config.class).add(Restrictions.eq("key", key)));
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
            config = dao.load(Config.class, mailSettingConfigId);
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
		dao.persist(config);

		for (ConfigListener listener: listenersProvider.get())
			listener.onSave(config);
	}

	@Sessional
	@Override
	public QosSetting getQosSetting() {
        Config config;
        if (qosSettingConfigId == null) {
    		config = getConfig(Key.QOS);
    		Preconditions.checkNotNull(config);
            qosSettingConfigId = config.getId();
        } else {
            config = dao.load(Config.class, qosSettingConfigId);
        }
        QosSetting setting = (QosSetting) config.getSetting();
        Preconditions.checkNotNull(setting);
        return setting;
	}

	@Transactional
	@Override
	public void saveQosSetting(QosSetting qosSetting) {
		Preconditions.checkNotNull(qosSetting);
		
		Config config = getConfig(Key.QOS);
		if (config == null) {
			config = new Config();
			config.setKey(Key.QOS);
		}
		config.setSetting(qosSetting);
		dao.persist(config);

		for (ConfigListener listener: listenersProvider.get())
			listener.onSave(config);
	}

}
