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

	private boolean trialExpireInOneWeekAlerted;

	private boolean trialExpiredAlerted;

	private boolean subscriptionExpireInOneMonthAlerted;

	private boolean subscriptionExpireInOneWeekAlerted;

	private boolean subscriptionExpiredAlerted;
	
	private boolean subscriptionExpiredBeforeReleaseDateAlerted;

	private boolean userLimitApproachingAlerted;

	private boolean userLimitExceededAlerted;
	
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
	
	public boolean isTrialExpireInOneWeekAlerted() {
		return trialExpireInOneWeekAlerted;
	}

	public void setTrialExpireInOneWeekAlerted(boolean trialExpireInOneWeekAlerted) {
		this.trialExpireInOneWeekAlerted = trialExpireInOneWeekAlerted;
	}

	public boolean isTrialExpiredAlerted() {
		return trialExpiredAlerted;
	}

	public void setTrialExpiredAlerted(boolean trialExpiredAlerted) {
		this.trialExpiredAlerted = trialExpiredAlerted;
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

	public boolean isSubscriptionExpiredBeforeReleaseDateAlerted() {
		return subscriptionExpiredBeforeReleaseDateAlerted;
	}

	public void setSubscriptionExpiredBeforeReleaseDateAlerted(boolean subscriptionExpiredBeforeReleaseDateAlerted) {
		this.subscriptionExpiredBeforeReleaseDateAlerted = subscriptionExpiredBeforeReleaseDateAlerted;
	}

	public boolean isUserLimitApproachingAlerted() {
		return userLimitApproachingAlerted;
	}

	public void setUserLimitApproachingAlerted(boolean userLimitApproachingAlerted) {
		this.userLimitApproachingAlerted = userLimitApproachingAlerted;
	}

	public boolean isUserLimitExceededAlerted() {
		return userLimitExceededAlerted;
	}

	public void setUserLimitExceededAlerted(boolean userLimitExceededAlerted) {
		this.userLimitExceededAlerted = userLimitExceededAlerted;
	}
	
	public void setAlerted(boolean alerted) {
		trialExpireInOneWeekAlerted = alerted;
		trialExpiredAlerted = alerted;
		subscriptionExpireInOneMonthAlerted = alerted;
		subscriptionExpireInOneWeekAlerted = alerted;
		subscriptionExpiredAlerted = alerted;
		subscriptionExpiredBeforeReleaseDateAlerted = alerted;
		userLimitApproachingAlerted = alerted;
		userLimitExceededAlerted = alerted;
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
