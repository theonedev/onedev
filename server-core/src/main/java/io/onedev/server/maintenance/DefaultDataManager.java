package io.onedev.server.maintenance;

import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.StringMode;
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
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.ZipUtils;
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
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.GlobalProjectSetting;
import io.onedev.server.model.support.administration.GlobalPullRequestSetting;
import io.onedev.server.model.support.administration.MailSetting;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.administration.SshSetting;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.model.support.administration.jobexecutor.AutoDiscoveredJobExecutor;
import io.onedev.server.notification.MailManager;
import io.onedev.server.persistence.PersistManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.util.init.ManualConfig;
import io.onedev.server.util.init.Skippable;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;

@Singleton
public class DefaultDataManager implements DataManager, Serializable {

	private final UserManager userManager;
	
	private final SettingManager settingManager;
	
	private final Validator validator;
	
	private final PersistManager persistManager;
	
	private final MailManager mailManager;
	
	private final PasswordService passwordService;
	
	private final TaskScheduler taskScheduler;
	
	private final RoleManager roleManager;
	
	private String backupTaskId;

	@Inject
	public DefaultDataManager(UserManager userManager, 
			SettingManager settingManager, PersistManager persistManager, 
			MailManager mailManager, Validator validator, TaskScheduler taskScheduler, 
			PasswordService passwordService, RoleManager roleManager) {
		this.userManager = userManager;
		this.settingManager = settingManager;
		this.validator = validator;
		this.taskScheduler = taskScheduler;
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
		User system = userManager.get(User.SYSTEM_ID);
		if (system == null) {
			system = new User();
			system.setId(User.SYSTEM_ID);
			system.setName(OneDev.NAME);
			system.setEmail("no email");
			system.setPassword("no password");
    		userManager.replicate(system);
		}
		User administrator = userManager.get(User.ROOT_ID);		
		if (administrator == null) {
			administrator = new User();
			administrator.setId(User.ROOT_ID);
			Set<String> excludedProperties = Sets.newHashSet("administrator", "canCreateProjects"); 
			manualConfigs.add(new ManualConfig("Create Administrator Account", null, administrator, excludedProperties) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					User user = (User) getSetting();
					user.setPassword(passwordService.encryptPassword(user.getPassword()));
		    		userManager.replicate(user);
				}
				
			});
		}

		Setting setting = settingManager.getSetting(Key.SYSTEM);
		SystemSetting systemSetting = null;
		Url webServerUrl = OneDev.getInstance().guessServerUrl(false);
		
		if (setting == null || setting.getValue() == null) {
		    systemSetting = new SystemSetting();
			systemSetting.setServerUrl(StringUtils.stripEnd(webServerUrl.toString(StringMode.FULL), "/"));
		} else if (!validator.validate(setting.getValue()).isEmpty()) {
			systemSetting = (SystemSetting) setting.getValue();
		}
		if (systemSetting != null) {
			Collection<String> excludedProps = new HashSet<>();
			if (Bootstrap.isInDocker()) {
				excludedProps.add("gitConfig");
				excludedProps.add("curlConfig");
			}
			manualConfigs.add(new ManualConfig("Specify System Setting", null, 
					systemSetting, excludedProps) {
	
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

		setting = settingManager.getSetting(Key.SSH);
		if (setting == null || setting.getValue() == null) {
			SshSetting sshSetting = new SshSetting();
			Url sshServerUrl = OneDev.getInstance().guessServerUrl(true);
            sshSetting.setServerUrl(StringUtils.stripEnd(sshServerUrl.toString(StringMode.FULL), "/"));
            sshSetting.setPemPrivateKey(SshKeyUtils.generatePEMPrivateKey());
            
            settingManager.saveSshSetting(sshSetting);
        }
		
		setting = settingManager.getSetting(Key.SECURITY);
		if (setting == null) {
			settingManager.saveSecuritySetting(new SecuritySetting());
		} 
		setting = settingManager.getSetting(Key.ISSUE);
		if (setting == null) {
			settingManager.saveIssueSetting(new GlobalIssueSetting());
		} 
		setting = settingManager.getSetting(Key.AUTHENTICATOR);
		if (setting == null) {
			settingManager.saveAuthenticator(null);
		}
		setting = settingManager.getSetting(Key.JOB_EXECUTORS);
		if (setting == null) {
			AutoDiscoveredJobExecutor executor = new AutoDiscoveredJobExecutor();
			executor.setName("auto-discovered");
			settingManager.saveJobExecutors(Lists.newArrayList(executor));
		}
		setting = settingManager.getSetting(Key.SSO_CONNECTORS);
		if (setting == null) {
			settingManager.saveSsoConnectors(Lists.newArrayList());
		}
		setting = settingManager.getSetting(Key.GROOVY_SCRIPTS);
		if (setting == null) {
			settingManager.saveGroovyScripts(Lists.newArrayList());
		}
		setting = settingManager.getSetting(Key.PULL_REQUEST);
		if (setting == null) {
			settingManager.savePullRequestSetting(new GlobalPullRequestSetting());
		}
		setting = settingManager.getSetting(Key.BUILD);
		if (setting == null) {
			settingManager.saveBuildSetting(new GlobalBuildSetting());
		}
		setting = settingManager.getSetting(Key.PROJECT);
		if (setting == null) {
			settingManager.saveProjectSetting(new GlobalProjectSetting());
		}
		
		setting = settingManager.getSetting(Key.MAIL);
		if (setting == null) {
			settingManager.saveMailSetting(null);
		} else if (setting.getValue() != null && !validator.validate(setting.getValue()).isEmpty()) {
			manualConfigs.add(new ManualConfig("Specify Mail Setting", null, setting.getValue()) {

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
			manualConfigs.add(new ManualConfig("Specify Backup Setting", null, backupSetting) {

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
		
		if (roleManager.get(Role.OWNER_ID) == null) {
			Role owner = new Role();
			owner.setName("Project Owner");
			owner.setId(Role.OWNER_ID);
			owner.setManageProject(true);
			roleManager.replicate(owner);
			roleManager.setupDefaults();
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
		String htmlBody = String.format(""
				+ "OneDev url: <a href='%s'>%s</a>"
				+ "<p style='margin: 16px 0;'>"
				+ "<b>Error detail:</b>"
				+ "<pre style='font-family: monospace;'>%s</pre>"
				+ "<p style='margin: 16px 0;'>"
				+ "-- Sent by OneDev", 
				url, url, Throwables.getStackTraceAsString(e));
		String textBody = String.format(""
				+ "OneDev url: %s\n\n"
				+ "Error detail:\n"
				+ "%s",
				url, Throwables.getStackTraceAsString(e));
		mailManager.sendMail(Lists.newArrayList(root.getEmail()), 
				"OneDev database auto-backup failed", htmlBody, textBody);
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(DataManager.class);
	}

}
