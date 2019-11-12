package io.onedev.server.maintenance;

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

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.ZipUtils;
import io.onedev.commons.utils.init.ManualConfig;
import io.onedev.commons.utils.init.Skippable;
import io.onedev.commons.utils.schedule.SchedulableTask;
import io.onedev.commons.utils.schedule.TaskScheduler;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.Role;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.BackupSetting;
import io.onedev.server.model.support.administration.BuildSetting;
import io.onedev.server.model.support.administration.IssueSetting;
import io.onedev.server.model.support.administration.MailSetting;
import io.onedev.server.model.support.administration.PullRequestSetting;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.model.support.administration.jobexecutor.AutoDiscoveredJobExecutor;
import io.onedev.server.model.support.role.CodePrivilege;
import io.onedev.server.model.support.role.JobPrivilege;
import io.onedev.server.notification.MailManager;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.PersistManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;

@Singleton
public class DefaultDataManager implements DataManager, Serializable {

	private final UserManager userManager;
	
	private final SettingManager settingManager;
	
	private final IdManager idManager;
	
	private final Validator validator;
	
	private final PersistManager persistManager;
	
	private final MailManager mailManager;
	
	private final PasswordService passwordService;
	
	private final TaskScheduler taskScheduler;
	
	private final RoleManager roleManager;
	
	private String backupTaskId;
	
	@Inject
	public DefaultDataManager(IdManager idManager, UserManager userManager, 
			SettingManager settingManager, PersistManager persistManager, 
			MailManager mailManager, Validator validator, TaskScheduler taskScheduler, 
			PasswordService passwordService, RoleManager roleManager) {
		this.userManager = userManager;
		this.settingManager = settingManager;
		this.validator = validator;
		this.taskScheduler = taskScheduler;
		this.idManager = idManager;
		this.persistManager = persistManager;
		this.mailManager = mailManager;
		this.passwordService = passwordService;
		this.roleManager = roleManager;
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
					user.setPassword(passwordService.encryptPassword(user.getPassword()));
					userManager.save(user, null);
					idManager.init(User.class);
				}
				
			});

			Role manager = new Role();
			manager.setName("Manager");
			manager.setManageProject(true);
			roleManager.save(manager, null);
			
			Role developer = new Role();
			developer.setName("Developer");
			developer.setCodePrivilege(CodePrivilege.WRITE);
			developer.setEditableIssueFields(Lists.newArrayList("Type", "Priority", "Assignee", "Resolution", "Duplicate With"));
			
			JobPrivilege jobPrivilege = new JobPrivilege();
			jobPrivilege.setJobNames("*");
			jobPrivilege.setRunJob(true);
			developer.getJobPrivileges().add(jobPrivilege);
			
			roleManager.save(developer, null);

			Role tester = new Role();
			tester.setName("Tester");
			tester.setCodePrivilege(CodePrivilege.READ);
			tester.setEditableIssueFields(Lists.newArrayList("Type", "Priority", "Assignee", "Resolution", "Duplicate With"));
			
			jobPrivilege = new JobPrivilege();
			jobPrivilege.setJobNames("*");
			jobPrivilege.setAccessLog(true);
			tester.getJobPrivileges().add(jobPrivilege);
			
			roleManager.save(tester, null);
			
			Role reporter = new Role();
			reporter.setName("Reporter");
			reporter.setCodePrivilege(CodePrivilege.NONE);
			reporter.setEditableIssueFields(Lists.newArrayList("Type", "Priority"));
			
			jobPrivilege = new JobPrivilege();
			jobPrivilege.setJobNames("*");
			reporter.getJobPrivileges().add(jobPrivilege);

			roleManager.save(reporter, null);
		}

		Setting setting = settingManager.getSetting(Key.SYSTEM);
		SystemSetting systemSetting = null;
		
		if (setting == null || setting.getValue() == null) {
			systemSetting = new SystemSetting();
			systemSetting.setServerUrl(OneDev.getInstance().guessServerUrl());
		} else {
			if (!validator.validate(setting.getValue()).isEmpty())
				systemSetting = (SystemSetting) setting.getValue();
		}
		if (systemSetting != null) {
			manualConfigs.add(new ManualConfig("Specify System Setting", systemSetting) {
	
				@Override
				public Skippable getSkippable() {
					return null;
				}
	
				@Override
				public void complete() {
					settingManager.saveSystemSetting((SystemSetting) getSetting());
				}
				
			});
		}

		setting = settingManager.getSetting(Key.SECURITY);
		if (setting == null) {
			settingManager.saveSecuritySetting(new SecuritySetting());
		} 
		setting = settingManager.getSetting(Key.ISSUE);
		if (setting == null) {
			settingManager.saveIssueSetting(new IssueSetting());
		} 
		setting = settingManager.getSetting(Key.AUTHENTICATOR);
		if (setting == null) {
			settingManager.saveAuthenticator(null);
		}
		setting = settingManager.getSetting(Key.JOB_EXECUTORS);
		if (setting == null) {
			settingManager.saveJobExecutors(Lists.newArrayList(new AutoDiscoveredJobExecutor()));
		}
		setting = settingManager.getSetting(Key.JOB_SCRIPTS);
		if (setting == null) {
			settingManager.saveGroovyScripts(Lists.newArrayList());
		}
		setting = settingManager.getSetting(Key.PULL_REQUEST);
		if (setting == null) {
			settingManager.savePullRequestSetting(new PullRequestSetting());
		}
		setting = settingManager.getSetting(Key.BUILD);
		if (setting == null) {
			settingManager.saveBuildSetting(new BuildSetting());
		}
		
		setting = settingManager.getSetting(Key.MAIL);
		if (setting == null) {
			settingManager.saveMailSetting(null);
		} else if (setting.getValue() != null && !validator.validate(setting.getValue()).isEmpty()) {
			manualConfigs.add(new ManualConfig("Specify Mail Setting", setting.getValue()) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					settingManager.saveMailSetting((MailSetting) getSetting());
				}
				
			});
		}
		
		setting = settingManager.getSetting(Key.BACKUP);
		if (setting == null) {
			settingManager.saveBackupSetting(null);
		} else if (setting.getValue() != null && !validator.validate(setting.getValue()).isEmpty()) {
			Serializable backupSetting = setting.getValue();
			manualConfigs.add(new ManualConfig("Specify Backup Setting", backupSetting) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					settingManager.saveBackupSetting((BackupSetting) getSetting());
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
					File tempDir = FileUtils.createTempDir("backup");
					try {
						File backupDir = new File(Bootstrap.getSiteDir(), Upgrade.DB_BACKUP_DIR);
						FileUtils.createDir(backupDir);
						persistManager.exportData(tempDir);
						File backupFile = new File(backupDir, 
								DateTimeFormat.forPattern(Upgrade.BACKUP_DATETIME_FORMAT).print(new DateTime()) + ".zip");
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
		scheduleBackup(settingManager.getBackupSetting());
	}
	
	@Sessional
	protected void notifyBackupError(Throwable e) {
		User root = userManager.getRoot();
		String url = settingManager.getSystemSetting().getServerUrl();
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
