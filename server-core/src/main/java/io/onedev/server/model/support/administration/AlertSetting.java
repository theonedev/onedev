package io.onedev.server.model.support.administration;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.UserChoice;
import io.onedev.server.util.usage.Usage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable
public class AlertSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean trialSubscriptionExpireInOneWeekAlerted;

	private boolean trialSubscriptionExpiredAlerted;

	private boolean subscriptionExpireInOneMonthAlerted;

	private boolean subscriptionExpireInOneWeekAlerted;

	private boolean subscriptionExpiredAlerted;
	
	private List<String> notifyUsers = new ArrayList<>();
	
	@Editable(description = "Select users to send alert email upon events such as database auto-backup failure, " +
			"cluster node unreachable etc")
	@UserChoice
	public List<String> getNotifyUsers() {
		return notifyUsers;
	}

	public void setNotifyUsers(List<String> notifyUsers) {
		this.notifyUsers = notifyUsers;
	}
	
	public boolean isTrialSubscriptionExpireInOneWeekAlerted() {
		return trialSubscriptionExpireInOneWeekAlerted;
	}

	public void setTrialSubscriptionExpireInOneWeekAlerted(boolean trialSubscriptionExpireInOneWeekAlerted) {
		this.trialSubscriptionExpireInOneWeekAlerted = trialSubscriptionExpireInOneWeekAlerted;
	}

	public boolean isTrialSubscriptionExpiredAlerted() {
		return trialSubscriptionExpiredAlerted;
	}

	public void setTrialSubscriptionExpiredAlerted(boolean trialSubscriptionExpiredAlerted) {
		this.trialSubscriptionExpiredAlerted = trialSubscriptionExpiredAlerted;
	}

	public boolean isSubscriptionExpireInOneMonthAlerted() {
		return subscriptionExpireInOneMonthAlerted;
	}

	public void setSubscriptionExpireInOneMonthAlerted(boolean subscriptionExpireInOneMonthAlerted) {
		this.subscriptionExpireInOneMonthAlerted = subscriptionExpireInOneMonthAlerted;
	}

	public boolean isSubscriptionExpireInOneWeekAlerted() {
		return subscriptionExpireInOneWeekAlerted;
	}

	public void setSubscriptionExpireInOneWeekAlerted(boolean subscriptionExpireInOneWeekAlerted) {
		this.subscriptionExpireInOneWeekAlerted = subscriptionExpireInOneWeekAlerted;
	}

	public boolean isSubscriptionExpiredAlerted() {
		return subscriptionExpiredAlerted;
	}

	public void setSubscriptionExpiredAlerted(boolean subscriptionExpiredAlerted) {
		this.subscriptionExpiredAlerted = subscriptionExpiredAlerted;
	}
	
	public void setAlerted(boolean alerted) {
		trialSubscriptionExpireInOneWeekAlerted = alerted;
		trialSubscriptionExpiredAlerted = alerted;
		subscriptionExpireInOneMonthAlerted = alerted;
		subscriptionExpireInOneWeekAlerted = alerted;
		subscriptionExpiredAlerted = alerted;
	}

	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (notifyUsers.contains(userName))
			usage.add("notify users");
		return usage.prefix("alert settings");
	}

	public void onRenameUser(String oldName, String newName) {
		var index = notifyUsers.indexOf(oldName);
		if (index != -1)
			notifyUsers.set(index, newName);
	}
	
}
