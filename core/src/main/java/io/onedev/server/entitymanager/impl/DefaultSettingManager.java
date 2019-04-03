package io.onedev.server.entitymanager.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.stringmatch.ChildAwareMatcher;
import io.onedev.commons.utils.stringmatch.Matcher;
import io.onedev.server.OneDev;
import io.onedev.server.cache.CommitInfoManager;
import io.onedev.server.data.DataManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Project;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.support.authenticator.Authenticator;
import io.onedev.server.model.support.jobexecutor.JobExecutor;
import io.onedev.server.model.support.setting.BackupSetting;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.model.support.setting.MailSetting;
import io.onedev.server.model.support.setting.SecuritySetting;
import io.onedev.server.model.support.setting.SystemSetting;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.patternset.PatternSet;

@Singleton
public class DefaultSettingManager extends AbstractEntityManager<Setting> implements SettingManager {
	
	private final DataManager dataManager;
	
	private final CommitInfoManager commitInfoManager;
	
	private volatile Long systemSettingId;
	
	private volatile Long mailSettingId;
	
	private volatile Long backupSettingId;
	
	private volatile Long securitySettingId;
	
	private volatile Long issueSettingId;
	
	private volatile Long authenticatorId;
	
	private volatile Long jobExecutorId;
	
	@Inject
	public DefaultSettingManager(Dao dao, DataManager dataManager, CommitInfoManager commitInfoManager) {
		super(dao);
		this.dataManager = dataManager;
		this.commitInfoManager = commitInfoManager;
	}

	@Sessional
	@Override
	public SystemSetting getSystemSetting() {
        Setting setting;
        if (systemSettingId == null) {
    		setting = getSetting(Key.SYSTEM);
    		Preconditions.checkNotNull(setting);
            systemSettingId = setting.getId();
        } else {
            setting = load(systemSettingId);
        }
        SystemSetting value = (SystemSetting) setting.getValue();
        Preconditions.checkNotNull(value);
        return value;
	}

	@Transactional
	@Override
	public void saveSystemSetting(SystemSetting systemSetting) {
		Preconditions.checkNotNull(systemSetting);
		
		Setting setting = getSetting(Key.SYSTEM);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.SYSTEM);
		}
		setting.setValue(systemSetting);
		dao.persist(setting);
	}

	@Sessional
	@Override
	public Setting getSetting(Key key) {
		return find(EntityCriteria.of(Setting.class).add(Restrictions.eq("key", key)));
	}

	@Sessional
	@Override
	public MailSetting getMailSetting() {
        Setting setting;
        if (mailSettingId == null) {
    		setting = getSetting(Key.MAIL);
    		Preconditions.checkNotNull(setting);
    		mailSettingId = setting.getId();
        } else {
            setting = load(mailSettingId);
        }
        return (MailSetting) setting.getValue();
	}

	@Transactional
	@Override
	public void saveMailSetting(MailSetting mailSetting) {
		Setting setting = getSetting(Key.MAIL);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.MAIL);
		}
		setting.setValue(mailSetting);
		dao.persist(setting);
	}

	@Sessional
	@Override
	public BackupSetting getBackupSetting() {
        Setting setting;
        if (backupSettingId == null) {
    		setting = getSetting(Key.BACKUP);
    		Preconditions.checkNotNull(setting);
    		backupSettingId = setting.getId();
        } else {
            setting = load(backupSettingId);
        }
        return (BackupSetting) setting.getValue();
	}

	@Transactional
	@Override
	public void saveBackupSetting(BackupSetting backupSetting) {
		Setting setting = getSetting(Key.BACKUP);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.BACKUP);
		}
		setting.setValue(backupSetting);
		dao.persist(setting);
		dataManager.scheduleBackup(backupSetting);
	}

	@Sessional
	@Override
	public SecuritySetting getSecuritySetting() {
        Setting setting;
        if (securitySettingId == null) {
    		setting = getSetting(Key.SECURITY);
    		Preconditions.checkNotNull(setting);
    		securitySettingId = setting.getId();
        } else {
            setting = load(securitySettingId);
        }
        return (SecuritySetting) setting.getValue();
	}

	@Transactional
	@Override
	public void saveIssueSetting(GlobalIssueSetting issueSetting) {
		Setting setting = getSetting(Key.ISSUE);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.ISSUE);
		}
		setting.setValue(issueSetting);
		dao.persist(setting);
	}
	
	@Sessional
	@Override
	public GlobalIssueSetting getIssueSetting() {
        Setting setting;
        if (issueSettingId == null) {
    		setting = getSetting(Key.ISSUE);
    		Preconditions.checkNotNull(setting);
    		issueSettingId = setting.getId();
        } else {
            setting = load(issueSettingId);
        }
        return (GlobalIssueSetting) setting.getValue();
	}

	@Transactional
	@Override
	public void saveSecuritySetting(SecuritySetting securitySetting) {
		Setting setting = getSetting(Key.SECURITY);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.SECURITY);
		}
		setting.setValue(securitySetting);
		dao.persist(setting);
	}
	
	@Sessional
	@Override
	public Authenticator getAuthenticator() {
        Setting setting;
        if (authenticatorId == null) {
    		setting = getSetting(Key.AUTHENTICATOR);
    		Preconditions.checkNotNull(setting);
    		authenticatorId = setting.getId();
        } else {
            setting = load(authenticatorId);
        }
        return (Authenticator) setting.getValue();
	}

	@Transactional
	@Override
	public void saveAuthenticator(Authenticator authenticator) {
		Setting setting = getSetting(Key.AUTHENTICATOR);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.AUTHENTICATOR);
		}
		setting.setValue(authenticator);
		dao.persist(setting);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JobExecutor> getJobExecutors() {
        Setting setting;
        if (jobExecutorId == null) {
    		setting = getSetting(Key.JOB_EXECUTORS);
    		Preconditions.checkNotNull(setting);
    		jobExecutorId = setting.getId();
        } else {
            setting = load(jobExecutorId);
        }
        return (List<JobExecutor>) setting.getValue();
	}

	@Override
	public void saveJobExecutors(List<JobExecutor> jobExecutors) {
		Setting setting = getSetting(Key.JOB_EXECUTORS);
		if (setting == null) {
			setting = new Setting();
			setting.setKey(Key.JOB_EXECUTORS);
		}
		setting.setValue((Serializable) jobExecutors);
		dao.persist(setting);
	}

	@Nullable
	public JobExecutor getJobExecutor(Project project, ObjectId commitId, String jobName, String image) {
		Matcher matcher = new ChildAwareMatcher();

		for (JobExecutor executor: OneDev.getInstance(SettingManager.class).getJobExecutors()) {
			if (executor.isEnabled() 
					&& PatternSet.fromString(executor.getProjects()).matches(matcher, project.getName())
					&& PatternSet.fromString(executor.getJobs()).matches(matcher, jobName)
					&& PatternSet.fromString(executor.getEnvironments()).matches(matcher, image)) {
				Collection<ObjectId> descendants = commitInfoManager.getDescendants(project, Sets.newHashSet(commitId));
				descendants.add(commitId);
			
				PatternSet branchPatterns = PatternSet.fromString(executor.getBranches());
				for (RefInfo ref: project.getBranches()) {
					String branchName = Preconditions.checkNotNull(GitUtils.ref2branch(ref.getRef().getName()));
					if (descendants.contains(ref.getPeeledObj()) && branchPatterns.matches(matcher, branchName))
						return executor;
				}
			} 
		}
		return null;
	}

	@Override
	public JobExecutor getJobExecutor(String runningInstance) {
		for (JobExecutor executor: getJobExecutors()) {
			if (executor.isEnabled() && executor.isRunning(runningInstance))
				return executor;
		}
		return null;
	}

}
