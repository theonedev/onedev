package com.pmease.gitop.core.manager.impl;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.loader.ManagedSerializedForm;
import com.pmease.commons.util.init.ManualConfig;
import com.pmease.commons.util.init.Skippable;
import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.core.manager.DataManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.setting.MailSetting;
import com.pmease.gitop.core.setting.SystemSetting;
import com.pmease.gitop.model.Config;
import com.pmease.gitop.model.Config.Key;
import com.pmease.gitop.model.User;

@Singleton
public class DefaultDataManager implements DataManager, Serializable {

	private final UserManager userManager;
	
	private final ConfigManager configManager;
	
	@Inject
	public DefaultDataManager(UserManager userManager, ConfigManager configManager) {
		this.userManager = userManager;
		this.configManager = configManager;
	}
	
	@SuppressWarnings("serial")
	@Transactional
	@Override
	public List<ManualConfig> init() {
		List<ManualConfig> manualConfigs = new ArrayList<ManualConfig>();
		User rootUser = userManager.find(null, new Order[]{Order.asc("id")});		
		if (rootUser == null) {
			manualConfigs.add(new ManualConfig("Create Administator Account", new User()) {

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
		
		Config systemConfig = configManager.getConfig(Key.SYSTEM);
		if (systemConfig == null || systemConfig.getSetting() == null) {
			manualConfigs.add(new ManualConfig("Specify System Setting", new SystemSetting()) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					configManager.saveSystemSetting((SystemSetting) getSetting());
				}
				
			});
		}
		
		Config mailConfig = configManager.getConfig(Key.MAIL);
		if (mailConfig == null) {
			manualConfigs.add(new ManualConfig("Specify Mail Setting", new MailSetting()) {

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

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(DataManager.class);
	}	
	
}
