package io.onedev.server.ee.subscription;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.entitymanager.AlertManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.io.Serializable;

@Singleton
public class EESubscriptionManager implements SchedulableTask, Serializable {
	
	private final TaskScheduler taskScheduler;
	
	private final AlertManager alertManager;
	
	private final SettingManager settingManager;
	
	private String taskId;
	
	@Inject
	public EESubscriptionManager(AlertManager alertManager, TaskScheduler taskScheduler, SettingManager settingManager) {
		this.taskScheduler = taskScheduler;
		this.alertManager = alertManager;
		this.settingManager = settingManager;
	}
	
	public final boolean isSubscriptionActive() {
		return SubscriptionSetting.load().isSubscriptionActive();
	}

	@Nullable
	public final String getLicensee() {
		var subscription = SubscriptionSetting.load().getSubscription();
		if (subscription != null)
			return subscription.getLicensee();
		else
			return null;
	}

	@Listen
	public final void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}
	
	@Listen
	public final void on(SystemStopping event) {
		if (taskId != null)
			taskScheduler.unschedule(taskId);
	}
	
	@Transactional
	@Override
	public final void execute() {
		var subscriptionSetting = SubscriptionSetting.load();
		var subscription = subscriptionSetting.getSubscription();
		if (subscription != null) {
			var alertSetting = settingManager.getAlertSetting();
			var now = new DateTime();
			var userDays = subscription.getUserDays();
			var userCount = subscription.countUsers();
			if (subscription.isTrial()) {
				if (userDays > 0)
					userDays--;
				var expirationDate = subscription.getExpirationDate(userCount);
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
				userDays -= userCount;
				if (userDays < 0)
					userDays = 0;
				var expirationDate = subscription.getExpirationDate(userCount);
				if (expirationDate == null) {
					if (!alertSetting.isSubscriptionExpiredAlerted()) {
						alertManager.alert("Enterprise features are disabled as subscription was expired. " +
								"Check <a href='/~administration/subscription-management'>subscription management</a> for details", null, false);
						alertSetting.setSubscriptionExpiredAlerted(true);
					}
				} else if (expirationDate.before(now.plusWeeks(1).toDate())) {
					if (!alertSetting.isSubscriptionExpireInOneWeekAlerted()) {
						alertManager.alert("Subscription will expire in one week with current number of users. " +
								"Check <a href='/~administration/subscription-management'>subscription management</a> for details", null, false);
						alertSetting.setSubscriptionExpireInOneWeekAlerted(true);
					}
				} else if (expirationDate.before(now.plusMonths(1).toDate())) {
					if (!alertSetting.isSubscriptionExpireInOneMonthAlerted()) {
						alertManager.alert("Subscription will expire in one month with current number of users. " +
								"Check <a href='/~administration/subscription-management'>subscription management</a> for details", null, false);
						alertSetting.setSubscriptionExpireInOneMonthAlerted(true);
					}
				}
			}
			subscription.setUserDays(userDays);
			subscriptionSetting.save();
		}
	}

	@Override
	public final ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.cronSchedule("0 0 1 * * ?");
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(EESubscriptionManager.class);
	}
	
}
