package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.bootstrap.BootstrapUtils;
import com.pmease.commons.hibernate.IdManager;
import com.pmease.commons.hibernate.PersistManager;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.loader.Listen;
import com.pmease.commons.loader.ManagedSerializedForm;
import com.pmease.commons.schedule.SchedulableTask;
import com.pmease.commons.schedule.TaskScheduler;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.init.ManualConfig;
import com.pmease.commons.util.init.Skippable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Config;
import com.pmease.gitplex.core.entity.Config.Key;
import com.pmease.gitplex.core.event.lifecycle.SystemStarting;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.DataManager;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.setting.BackupSetting;
import com.pmease.gitplex.core.setting.MailSetting;
import com.pmease.gitplex.core.setting.SystemSetting;

@Singleton
public class DefaultDataManager implements DataManager, Serializable {

	private static final String BACKUP_DATETIME_FORMAT = "yyyy-MM-dd_HH-mm-ss";
	
	private final AccountManager accountManager;
	
	private final ConfigManager configManager;
	
	private final IdManager idManager;
	
	private final TaskScheduler taskScheduler;
	
	private final Validator validator;
	
	private final PersistManager persistManager;
	
	private final MailManager mailManager;
	
	private String backupTaskId;
	
	@Inject
	public DefaultDataManager(IdManager idManager, AccountManager accountManager, 
			ConfigManager configManager, TaskScheduler taskScheduler, 
			PersistManager persistManager, MailManager mailManager, 
			Validator validator) {
		this.accountManager = accountManager;
		this.configManager = configManager;
		this.validator = validator;
		this.idManager = idManager;
		this.taskScheduler = taskScheduler;
		this.persistManager = persistManager;
		this.mailManager = mailManager;
	}
	
	@SuppressWarnings("serial")
	@Transactional
	@Override
	public List<ManualConfig> init() {
		List<ManualConfig> manualConfigs = new ArrayList<ManualConfig>();
		Account administrator = accountManager.get(Account.ADMINISTRATOR_ID);		
		if (administrator == null) {
			administrator = new Account();
			administrator.setId(Account.ADMINISTRATOR_ID);
			manualConfigs.add(new ManualConfig("Create Administator Account", administrator, 
					Sets.newHashSet("description", "defaultPrivilege")) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					accountManager.save((Account) getSetting(), null);
					idManager.init(Account.class);
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
		
		Config backupConfig = configManager.getConfig(Key.BACKUP);
		if (backupConfig == null) {
			configManager.saveBackupSetting(null);
		} else if (backupConfig.getSetting() != null && !validator.validate(backupConfig.getSetting()).isEmpty()) {
			Serializable backupSetting = backupConfig.getSetting();
			manualConfigs.add(new ManualConfig("Specify Backup Setting", backupSetting) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					configManager.saveBackupSetting((BackupSetting) getSetting());
				}
				
			});
		}
		
		return manualConfigs;
	}

	@Override
	public void scheduleBackup(BackupSetting backupSetting) {
		if (backupTaskId != null)
			taskScheduler.unschedule(backupTaskId);
		if (backupSetting != null) { 
			backupTaskId = taskScheduler.schedule(new SchedulableTask() {

				@Override
				public void execute() {
					File tempDir = BootstrapUtils.createTempDir("backup");
					try {
						File backupDir = new File(backupSetting.getFolder());
						if (!backupDir.isAbsolute()) 
							backupDir = new File(Bootstrap.installDir, backupSetting.getFolder());
						if (!backupDir.exists()) {
							throw new RuntimeException("Backup directory does not exist: " + backupDir.getAbsolutePath());
						}
						persistManager.exportData(tempDir);
						File backupFile = new File(backupDir, 
								DateTimeFormat.forPattern(BACKUP_DATETIME_FORMAT).print(new DateTime()) + ".zip");
						BootstrapUtils.zip(tempDir, backupFile);
					} catch (Exception e) {
						notifyBackupError(e);
						throw Throwables.propagate(e);
					} finally {
						FileUtils.deleteDir(tempDir);
					}
				}

				@Override
				public ScheduleBuilder<?> getScheduleBuilder() {
					return CronScheduleBuilder.cronSchedule(backupSetting.getSchedule());
				}
				
			});
		}
	}
	
	@Listen
	public void on(SystemStarting event) {
		scheduleBackup(configManager.getBackupSetting());
	}
	
	@Sessional
	protected void notifyBackupError(Exception e) {
		Account root = accountManager.getRoot();
		String url = configManager.getSystemSetting().getServerUrl();
		String body = String.format(""
				+ "GitPlex url: <a href='%s'>%s</a>"
				+ "<p style='margin: 16px 0;'>"
				+ "<b>Error detail:</b>"
				+ "<pre style='font-family: monospace;'>%s</pre>"
				+ "<p style='margin: 16px 0;'>"
				+ "-- Sent by GitPlex", 
				url, url, Throwables.getStackTraceAsString(e));
		mailManager.sendMail(Lists.newArrayList(root), "GitPlex database auto-backup failed", body);
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(DataManager.class);
	}

}
