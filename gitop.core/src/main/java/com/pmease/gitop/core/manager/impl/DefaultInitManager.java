package com.pmease.gitop.core.manager.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;

import com.pmease.commons.persistence.Transactional;
import com.pmease.gitop.core.ManualConfig;
import com.pmease.gitop.core.Skippable;
import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.core.manager.InitManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.Config;
import com.pmease.gitop.core.model.Config.Key;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.setting.MailSetting;
import com.pmease.gitop.core.setting.StorageSetting;

@Singleton
public class DefaultInitManager implements InitManager {

	private final UserManager userManager;
	
	private final ConfigManager configManager;
	
	@Inject
	public DefaultInitManager(UserManager userManager, ConfigManager configManager) {
		this.userManager = userManager;
		this.configManager = configManager;
	}
	
	@Transactional
	@Override
	public List<ManualConfig> init() {
		List<ManualConfig> manualConfigs = new ArrayList<ManualConfig>();
		User rootUser = userManager.find(null, new Order[]{Order.asc("id")});		
		if (rootUser == null) {
			manualConfigs.add(new ManualConfig(new User()) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					userManager.save((User) getSetting());
				}
				
			});
		}
		
		Config storageConfig = configManager.getConfig(Key.STORAGE);
		if (storageConfig == null || storageConfig.getSetting() == null) {
			manualConfigs.add(new ManualConfig(new StorageSetting()) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					configManager.saveStorageSetting((StorageSetting) getSetting());
				}
				
			});
		}
		
		Config mailConfig = configManager.getConfig(Key.MAIL);
		if (mailConfig == null) {
			manualConfigs.add(new ManualConfig(new MailSetting()) {

				@Override
				public Skippable getSkippable() {
					return new Skippable() {

						@Override
						public void skip() {
							configManager.saveMailSetting(null);
						}
						
					};
				}

				@Override
				public void complete() {
					configManager.saveMailSetting((MailSetting) getSetting());
				}
				
			});
		}
		
		return manualConfigs;
	}
	
}
