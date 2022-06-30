package io.onedev.server.maintenance;

import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

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

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.Listen;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Role;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.AgentSetting;
import io.onedev.server.model.support.administration.BackupSetting;
import io.onedev.server.model.support.administration.BrandingSetting;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.GlobalProjectSetting;
import io.onedev.server.model.support.administration.GlobalPullRequestSetting;
import io.onedev.server.model.support.administration.GpgSetting;
import io.onedev.server.model.support.administration.MailSetting;
import io.onedev.server.model.support.administration.PerformanceSetting;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.administration.ServiceDeskSetting;
import io.onedev.server.model.support.administration.SshSetting;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.model.support.administration.notificationtemplate.NotificationTemplateSetting;
import io.onedev.server.model.support.issue.LinkSpecOpposite;
import io.onedev.server.notification.MailManager;
import io.onedev.server.persistence.PersistManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.util.init.ManualConfig;
import io.onedev.server.util.init.Skippable;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;
import io.onedev.server.web.util.editablebean.NewUserBean;

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
	
	private final LinkSpecManager linkSpecManager;
	
	private final EmailAddressManager emailAddressManager;
	
	private String backupTaskId;

	@Inject
	public DefaultDataManager(UserManager userManager, 
			SettingManager settingManager, PersistManager persistManager, 
			MailManager mailManager, Validator validator, TaskScheduler taskScheduler, 
			PasswordService passwordService, RoleManager roleManager, 
			LinkSpecManager linkSpecManager, EmailAddressManager emailAddressManager) {
		this.userManager = userManager;
		this.settingManager = settingManager;
		this.validator = validator;
		this.taskScheduler = taskScheduler;
		this.persistManager = persistManager;
		this.mailManager = mailManager;
		this.passwordService = passwordService;
		this.roleManager = roleManager;
		this.linkSpecManager = linkSpecManager;
		this.emailAddressManager = emailAddressManager;
	}

	@SuppressWarnings({"serial"})
	@Transactional
	@Override
	public List<ManualConfig> init() {
		List<ManualConfig> manualConfigs = new ArrayList<ManualConfig>();
		User system = userManager.get(User.ONEDEV_ID);
		if (system == null) {
			system = new User();
			system.setId(User.ONEDEV_ID);
			system.setName(User.ONEDEV_NAME.toLowerCase());
			system.setFullName(User.ONEDEV_NAME);
			system.setPassword("no password");
    		userManager.replicate(system);
		}
		User unknown = userManager.get(User.UNKNOWN_ID);
		if (unknown == null) {
			unknown = new User();
			unknown.setId(User.UNKNOWN_ID);
			unknown.setName(User.UNKNOWN_NAME.toLowerCase());
			unknown.setFullName(User.UNKNOWN_NAME);
			unknown.setPassword("no password");
    		userManager.replicate(unknown);
		}
		if (userManager.get(User.ROOT_ID) == null) {
			manualConfigs.add(new ManualConfig("Create Administrator Account", null, new NewUserBean()) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					NewUserBean newUserBean = (NewUserBean) getSetting();
					User user = new User();
					user.setId(User.ROOT_ID);
					user.setName(newUserBean.getName());
					user.setFullName(newUserBean.getFullName());
					user.setPassword(passwordService.encryptPassword(newUserBean.getPassword()));
		    		userManager.replicate(user);

		    		EmailAddress primaryEmailAddress = null;
		    		for (EmailAddress emailAddress: emailAddressManager.query()) { 
		    			if (emailAddress.getOwner().equals(user) && emailAddress.isPrimary()) {
		    				primaryEmailAddress = emailAddress;
		    				break;
		    			}
		    		}
		    		
		    		if (primaryEmailAddress == null) {
			    		primaryEmailAddress = new EmailAddress();
			    		primaryEmailAddress.setPrimary(true);
			    		primaryEmailAddress.setGit(true);
			    		primaryEmailAddress.setVerificationCode(null);
			    		primaryEmailAddress.setOwner(user);
		    		}
		    		primaryEmailAddress.setValue(newUserBean.getEmailAddress());
		    		emailAddressManager.save(primaryEmailAddress);
				}
				
			});
		}

		Setting setting = settingManager.getSetting(Key.SYSTEM);
		SystemSetting systemSetting = null;

		String ingressUrl = OneDev.getInstance().getIngressUrl();
		
		if (setting == null || setting.getValue() == null) {
		    systemSetting = new SystemSetting();
			if (ingressUrl != null) {
				systemSetting.setServerUrl(ingressUrl);
				settingManager.saveSystemSetting(systemSetting);
				systemSetting = null;
			} else {
				systemSetting.setServerUrl(OneDev.getInstance().guessServerUrl());
			}
		} else {
			systemSetting = (SystemSetting) setting.getValue();
			if (ingressUrl != null)
				systemSetting.setServerUrl(ingressUrl);
			if (validator.validate(systemSetting).isEmpty())			
				systemSetting = null;
		}
		
		if (systemSetting != null) {
			Collection<String> excludedProps = Sets.newHashSet("sshRootUrl", "gravatarEnabled");
			if (Bootstrap.isInDocker()) {
				excludedProps.add("gitConfig");
				excludedProps.add("curlConfig");
			}
			if (ingressUrl != null)
				excludedProps.add("serverUrl");

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
            sshSetting.setPemPrivateKey(SshKeyUtils.generatePEMPrivateKey());
            
            settingManager.saveSshSetting(sshSetting);
        }
		
		setting = settingManager.getSetting(Key.GPG);
		if (setting == null || setting.getValue() == null) {
			GpgSetting gpgSetting = new GpgSetting();
            settingManager.saveGpgSetting(gpgSetting);
        }
		
		setting = settingManager.getSetting(Key.SECURITY);
		if (setting == null) {
			settingManager.saveSecuritySetting(new SecuritySetting());
		} 
		setting = settingManager.getSetting(Key.ISSUE);
		if (setting == null) {
			LinkSpec link = new LinkSpec();
			link.setName("Child Issue");
			link.setMultiple(true);
			link.setOpposite(new LinkSpecOpposite());
			link.getOpposite().setName("Parent Issue");
			link.setOrder(1);
			linkSpecManager.save(link);
			
			link = new LinkSpec();
			link.setName("Blocked By");
			link.setMultiple(true);
			link.setOpposite(new LinkSpecOpposite());
			link.getOpposite().setName("Blocking");
			link.getOpposite().setMultiple(true);
			link.setOrder(2);
			linkSpecManager.save(link);
			
			settingManager.saveIssueSetting(new GlobalIssueSetting());
		} 
		setting = settingManager.getSetting(Key.PERFORMANCE);
		if (setting == null) {
			settingManager.savePerformanceSetting(new PerformanceSetting());
		} 
		setting = settingManager.getSetting(Key.AUTHENTICATOR);
		if (setting == null) {
			settingManager.saveAuthenticator(null);
		}
		setting = settingManager.getSetting(Key.JOB_EXECUTORS);
		if (setting == null) 
			settingManager.saveJobExecutors(new ArrayList<>());
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
		setting = settingManager.getSetting(Key.AGENT);
		if (setting == null) {
			settingManager.saveAgentSetting(new AgentSetting());
		}
		setting = settingManager.getSetting(Key.SERVICE_DESK_SETTING);
		if (setting == null) { 
			settingManager.saveServiceDeskSetting(null);
		} else if (setting.getValue() != null && !validator.validate(setting.getValue()).isEmpty()) {
			manualConfigs.add(new ManualConfig("Specify Service Desk Setting", null, 
					setting.getValue(), new HashSet<>(), true) {
	
				@Override
				public Skippable getSkippable() {
					return null;
				}
	
				@Override
				public void complete() {
					settingManager.saveServiceDeskSetting((ServiceDeskSetting) getSetting());
				}
				
			});
		}
		setting = settingManager.getSetting(Key.NOTIFICATION_TEMPLATE_SETTING);
		if (setting == null) {
			settingManager.saveNotificationTemplateSetting(new NotificationTemplateSetting());
		}
		
		setting = settingManager.getSetting(Key.CONTRIBUTED_SETTINGS);
		if (setting == null) 
			settingManager.saveContributedSettings(new LinkedHashMap<>());
		
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
		
		setting = settingManager.getSetting(Key.BRANDING);
		if (setting == null) 
			settingManager.saveBrandingSetting(new BrandingSetting());
		
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
						FileUtils.zip(tempDir, backupFile, null);
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
		
		EmailAddress emailAddress = root.getPrimaryEmailAddress();
		if (emailAddress != null && emailAddress.isVerified()) {
			mailManager.sendMail(Lists.newArrayList(emailAddress.getValue()), Lists.newArrayList(),
					Lists.newArrayList(), "[Backup] OneDev Database Auto-backup Failed", 
					htmlBody, textBody, null, null);
		}
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(DataManager.class);
	}

}
