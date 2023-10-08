package io.onedev.server.ee.subscription;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.manager.AlertManager;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.util.WicketUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.io.Serializable;

@Singleton
public class DefaultSubscriptionManager implements SubscriptionManager, SchedulableTask, Serializable {
	
	private final TaskScheduler taskScheduler;
	
	private final AlertManager alertManager;
	
	private final SettingManager settingManager;
	
	private final GroupManager groupManager;
	
	private String taskId;
	
	@Inject
	public DefaultSubscriptionManager(AlertManager alertManager, TaskScheduler taskScheduler, 
									  SettingManager settingManager, GroupManager groupManager) {
		this.taskScheduler = taskScheduler;
		this.alertManager = alertManager;
		this.settingManager = settingManager;
		this.groupManager = groupManager;
	}
	
	@Override
	public boolean isSubscriptionActive() {
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
			int userCount = subscription.countUsers();
			var userDays = subscription.getUserDays();
			if (subscription.isTrial()) {
				if (userDays > 0)
					userDays--;
				var expirationDate = subscription.getExpirationDate();
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
				if (subscription.getLicenseGroup() != null
						&& groupManager.find(subscription.getLicenseGroup()) == null) {
					alertManager.alert(
							"License group '" + subscription.getLicenseGroup() + "' not found",
							"Remaining user month calculation will be based on all users in system");
				}
				userDays -= userCount;
				if (userDays < 0)
					userDays = 0;
				var expirationDate = subscription.getExpirationDate();
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
	public Component renderSupportRequestLink(String componentId) {
		if (WicketUtils.isSubscriptionActive() && !"code.onedev.io".equals(getLicensee())) {
			return new ModalLink(componentId) {

				@Override
				protected Component newContent(String id, ModalPanel modal) {
					return new SupportRequestPanel(id, modal);
				}

			};
		} else {
			return new WebMarkupContainer(componentId).setVisible(false);
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.cronSchedule("0 0 1 * * ?");
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(DefaultSubscriptionManager.class);
	}
	
}
