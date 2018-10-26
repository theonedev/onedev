package io.onedev.server.manager.impl;

import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import org.apache.shiro.authc.credential.PasswordService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.launcher.bootstrap.Bootstrap;
import io.onedev.launcher.loader.Listen;
import io.onedev.launcher.loader.ManagedSerializedForm;
import io.onedev.server.OneDev;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.manager.DataManager;
import io.onedev.server.manager.MailManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.User;
import io.onedev.server.model.support.setting.BackupSetting;
import io.onedev.server.model.support.setting.MailSetting;
import io.onedev.server.model.support.setting.SecuritySetting;
import io.onedev.server.model.support.setting.SystemSetting;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.PersistManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.utils.ExceptionUtils;
import io.onedev.utils.FileUtils;
import io.onedev.utils.ZipUtils;
import io.onedev.utils.init.ManualConfig;
import io.onedev.utils.init.Skippable;
import io.onedev.utils.schedule.SchedulableTask;
import io.onedev.utils.schedule.TaskScheduler;

@Singleton
public class DefaultDataManager implements DataManager, Serializable {

	private static final String BACKUP_DATETIME_FORMAT = "yyyy-MM-dd_HH-mm-ss";
	
	private final UserManager userManager;
	
	private final SettingManager configManager;
	
	private final IdManager idManager;
	
	private final Validator validator;
	
	private final PersistManager persistManager;
	
	private final MailManager mailManager;
	
	private final PasswordService passwordService;
	
	private final TaskScheduler taskScheduler;
	
	private String backupTaskId;
	
	@Inject
	public DefaultDataManager(IdManager idManager, UserManager userManager, 
			SettingManager configManager, PersistManager persistManager, 
			MailManager mailManager, Validator validator, TaskScheduler taskScheduler, 
			PasswordService passwordService) {
		this.userManager = userManager;
		this.configManager = configManager;
		this.validator = validator;
		this.taskScheduler = taskScheduler;
		this.idManager = idManager;
		this.persistManager = persistManager;
		this.mailManager = mailManager;
		this.passwordService = passwordService;
	}
	
	@SuppressWarnings("serial")
	@Transactional
	@Override
	public List<ManualConfig> init() {
		List<ManualConfig> manualConfigs = new ArrayList<ManualConfig>();
		User administrator = userManager.get(User.ROOT_ID);		
		if (administrator == null) {
			administrator = new User();
			administrator.setId(User.ROOT_ID);
			Set<String> excludedProperties = Sets.newHashSet("administrator", "canCreateProjects"); 
			manualConfigs.add(new ManualConfig("Create Administator User", administrator, excludedProperties) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					User user = (User) getSetting();
					user.setPassword(passwordService.encryptPassword("admin"));
					userManager.save(user, null);
					idManager.init(User.class);
				}
				
			});
		}

		Setting systemConfig = configManager.getSetting(Key.SYSTEM);
		SystemSetting systemSetting = null;
		
		if (systemConfig == null || systemConfig.getValue() == null) {
			systemSetting = new SystemSetting();
			systemSetting.setServerUrl(OneDev.getInstance().guessServerUrl());
		} else {
			if (!validator.validate(systemConfig.getValue()).isEmpty())
				systemSetting = (SystemSetting) systemConfig.getValue();
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

		Setting securityConfig = configManager.getSetting(Key.SECURITY);
		if (securityConfig == null) {
			configManager.saveSecuritySetting(new SecuritySetting());
		} 
		Setting authenticatorConfig = configManager.getSetting(Key.AUTHENTICATOR);
		if (authenticatorConfig == null) {
			configManager.saveAuthenticator(null);
		}
		
		Setting mailConfig = configManager.getSetting(Key.MAIL);
		if (mailConfig == null) {
			configManager.saveMailSetting(null);
		} else if (mailConfig.getValue() != null && !validator.validate(mailConfig.getValue()).isEmpty()) {
			manualConfigs.add(new ManualConfig("Specify Mail Setting", mailConfig.getValue()) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					configManager.saveMailSetting((MailSetting) getSetting());
				}
				
			});
		}
		
		Setting backupConfig = configManager.getSetting(Key.BACKUP);
		if (backupConfig == null) {
			configManager.saveBackupSetting(null);
		} else if (backupConfig.getValue() != null && !validator.validate(backupConfig.getValue()).isEmpty()) {
			Serializable backupSetting = backupConfig.getValue();
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
		
		Setting licenseKeyConfig = configManager.getSetting(Key.LICENSE);
		if (licenseKeyConfig == null) {
			configManager.saveLicense(null);
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
					File tempDir = FileUtils.createTempDir("backup");
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
						ZipUtils.zip(tempDir, backupFile);
					} catch (Exception e) {
						notifyBackupError(e);
						throw ExceptionUtils.unchecked(e);
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
		User root = userManager.getRoot();
		String url = configManager.getSystemSetting().getServerUrl();
		String body = String.format(""
				+ "OneDev url: <a href='%s'>%s</a>"
				+ "<p style='margin: 16px 0;'>"
				+ "<b>Error detail:</b>"
				+ "<pre style='font-family: monospace;'>%s</pre>"
				+ "<p style='margin: 16px 0;'>"
				+ "-- Sent by OneDev", 
				url, url, Throwables.getStackTraceAsString(e));
		mailManager.sendMail(Lists.newArrayList(root.getEmail()), "OneDev database auto-backup failed", body);
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(DataManager.class);
	}

}
