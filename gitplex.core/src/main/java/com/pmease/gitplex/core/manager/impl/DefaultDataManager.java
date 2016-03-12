package com.pmease.gitplex.core.manager.impl;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import com.google.common.collect.Sets;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.ManagedSerializedForm;
import com.pmease.commons.util.init.ManualConfig;
import com.pmease.commons.util.init.Skippable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Config;
import com.pmease.gitplex.core.entity.Config.Key;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.DataManager;
import com.pmease.gitplex.core.setting.MailSetting;
import com.pmease.gitplex.core.setting.SystemSetting;

@Singleton
public class DefaultDataManager implements DataManager, Serializable {

	private final Dao dao;
	
	private final AccountManager userManager;
	
	private final ConfigManager configManager;
	
	private final Validator validator;
	
	@Inject
	public DefaultDataManager(Dao dao, AccountManager userManager, ConfigManager configManager, Validator validator) {
		this.dao = dao;
		this.userManager = userManager;
		this.configManager = configManager;
		this.validator = validator;
	}
	
	@SuppressWarnings("serial")
	@Transactional
	@Override
	public List<ManualConfig> init() {
		List<ManualConfig> manualConfigs = new ArrayList<ManualConfig>();
		Account rootUser = dao.get(Account.class, Account.ADMINISTRATOR_ID);		
		if (rootUser == null) {
			rootUser = new Account();
			rootUser.setId(Account.ADMINISTRATOR_ID);
			manualConfigs.add(new ManualConfig("Create Administator Account", rootUser, 
					Sets.newHashSet("description", "defaultPrivilege")) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					userManager.save((Account) getSetting(), null);
				}
				
			});
		}

		Config systemConfig = configManager.getConfig(Key.SYSTEM);
		SystemSetting systemSetting = null;
		
		if (systemConfig == null || systemConfig.getSetting() == null) {
			systemSetting = new SystemSetting();
			systemSetting.setServerUrl(GitPlex.getInstance().guessServerUrl());
		} else {
			if (!validator.validate(systemConfig.getSetting()).isEmpty())
				systemSetting = (SystemSetting) systemConfig.getSetting();
		}
		if (systemSetting != null) {
			manualConfigs.add(new ManualConfig("Specify System Setting", systemSetting) {
	
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
		Serializable mailSetting = null;
		if (mailConfig == null) {
			mailSetting = new MailSetting();
		} else if (mailConfig.getSetting() != null) {
			if (!validator.validate(mailConfig.getSetting()).isEmpty())
				mailSetting = mailConfig.getSetting();
		}
		
		if (mailSetting != null) {
			manualConfigs.add(new ManualConfig("Specify Mail Setting", mailSetting) {

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
