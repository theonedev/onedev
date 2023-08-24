package io.onedev.server.ee.subscription;

import com.google.common.collect.Sets;
import io.onedev.server.entitymanager.*;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Singleton
public class DefaultSubscriptionManager implements SubscriptionManager, SchedulableTask {
	
	private final UserManager userManager;
	
	private final TaskScheduler taskScheduler;
	
	private final AlertManager alertManager;
	
	private final SettingManager settingManager;
	
	private final UserAuthorizationManager userAuthorizationManager;
	
	private final GroupAuthorizationManager groupAuthorizationManager;
	
	private final MembershipManager membershipManager;
	
	private final ProjectManager projectManager;
	
	private final GroupManager groupManager;
	
	private String taskId;
	
	@Inject
	public DefaultSubscriptionManager(UserManager userManager, AlertManager alertManager,
									  TaskScheduler taskScheduler, SettingManager settingManager,
									  UserAuthorizationManager userAuthorizationManager,
									  GroupAuthorizationManager groupAuthorizationManager,
									  MembershipManager membershipManager, ProjectManager projectManager, 
									  GroupManager groupManager) {
		this.userManager = userManager;
		this.taskScheduler = taskScheduler;
		this.alertManager = alertManager;
		this.settingManager = settingManager;
		this.userAuthorizationManager = userAuthorizationManager;
		this.groupAuthorizationManager = groupAuthorizationManager;
		this.membershipManager = membershipManager;
		this.projectManager = projectManager;
		this.groupManager = groupManager;
	}
	
	@Override
	public boolean isActive() {
		var subscription = SubscriptionSetting.load().getSubscription();
		if (subscription != null) 
			return subscription.getUserDays() > 0;
		else 
			return false;
	}

	@Nullable
	@Override
	public String getLicensee() {
		var subscription = SubscriptionSetting.load().getSubscription();
		if (subscription != null)
			return subscription.getLicensee();
		else
			return null;
	}

	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}
	
	@Listen
	public void on(SystemStopping event) {
		if (taskId != null)
			taskScheduler.unschedule(taskId);
	}
	
	@Transactional
	@Override
	public void execute() {
		var subscriptionSetting = SubscriptionSetting.load();
		var subscription = subscriptionSetting.getSubscription();
		if (subscription != null) {
			var alertSetting = settingManager.getAlertSetting();
			var now = new DateTime();
			var developerCount = countDevelopers();
			var userDays = subscription.getUserDays();
			if (subscription.isTrial()) {
				if (userDays > 0)
					userDays--;
				var expirationDate = subscription.getExpirationDate(developerCount);
				if (expirationDate == null) {
					if (!alertSetting.isTrialSubscriptionExpiredAlerted()) {
						alertManager.alert("Enterprise features are disabled as trial subscription was expired. " +
								"Check <a href='/~administration/subscription-management'>subscription management</a> for details", null, false);
						alertSetting.setTrialSubscriptionExpiredAlerted(true);
					} 
				} else if (expirationDate.before(now.plusWeeks(1).toDate())) {
					if (!alertSetting.isTrialSubscriptionExpireInOneWeekAlerted()) {
						alertManager.alert("Trial subscription will expire in one week. " +
								"Check <a href='/~administration/subscription-management'>subscription management</a> for details", null, false);
						alertSetting.setTrialSubscriptionExpireInOneWeekAlerted(true);
					}
				}
			} else {
				userDays -= developerCount;
				if (userDays < 0)
					userDays = 0;
				var expirationDate = subscription.getExpirationDate(developerCount);
				if (expirationDate == null) {
					if (!alertSetting.isSubscriptionExpiredAlerted()) {
						alertManager.alert("Enterprise features are disabled as subscription was expired. " +
								"Check <a href='/~administration/subscription-management'>subscription management</a> for details", null, false);
						alertSetting.setSubscriptionExpiredAlerted(true);
					}
				} else if (expirationDate.before(now.plusWeeks(1).toDate())) {
					if (!alertSetting.isSubscriptionExpireInOneWeekAlerted()) {
						alertManager.alert("Subscription will expire in one week with current number of developers. " +
								"Check <a href='/~administration/subscription-management'>subscription management</a> for details", null, false);
						alertSetting.setSubscriptionExpireInOneWeekAlerted(true);
					}
				} else if (expirationDate.before(now.plusMonths(1).toDate())) {
					if (!alertSetting.isSubscriptionExpireInOneMonthAlerted()) {
						alertManager.alert("Subscription will expire in one month with current number of developers. " +
								"Check <a href='/~administration/subscription-management'>subscription management</a> for details", null, false);
						alertSetting.setSubscriptionExpireInOneMonthAlerted(true);
					}
				}
			}
			subscription.setUserDays(userDays);
			subscriptionSetting.save();
		}
	}

	@Sessional
	@Override
	public int countDevelopers() {
		for (var project: projectManager.query()) {
			if (project.getDefaultRole() != null && project.getDefaultRole().implies(new WriteCode()))
				return userManager.count();
		}

		Group defaultLoginGroup = settingManager.getSecuritySetting().getDefaultLoginGroup();
		if (defaultLoginGroup != null) {
			if (defaultLoginGroup.isAdministrator())
				return userManager.count();
			for (var authorization: defaultLoginGroup.getAuthorizations()) {
				if (authorization.getRole().implies(new WriteCode()))
					return userManager.count();
			}
		}

		Collection<User> developers = Sets.newHashSet(userManager.getRoot());

		Map<Group, Collection<User>> members = new HashMap<>();
		for (var membership: membershipManager.query())
			members.computeIfAbsent(membership.getGroup(), k -> new HashSet<>()).add(membership.getUser());

		for (var group: groupManager.query()) {
			if (group.isAdministrator()) {
				var membersOfGroup = members.get(group);
				if (membersOfGroup != null)
					developers.addAll(membersOfGroup);
			}
		}
		for (var authorization: groupAuthorizationManager.query()) {
			if (authorization.getRole().implies(new WriteCode())) {
				var membersOfGroup = members.get(authorization.getGroup());
				if (membersOfGroup != null)
					developers.addAll(membersOfGroup);
			}
		}

		for (var authorization: userAuthorizationManager.query()) {
			if (authorization.getRole().implies(new WriteCode()))
				developers.add(authorization.getUser());
		}

		return developers.size();
	}
	
	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.cronSchedule("0 0 1 * * ?");
	}
	
}
