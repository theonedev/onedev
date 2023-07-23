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

	private boolean trialEnterpriseLicenseExpireInOneWeekAlerted;

	private boolean trialEnterpriseLicenseExpiredAlerted;

	private boolean enterpriseLicenseExpireInOneMonthAlerted;

	private boolean enterpriseLicenseExpireInOneWeekAlerted;

	private boolean enterpriseLicenseExpiredAlerted;

	private boolean enterpriseLicenseUserLimitApproachingAlerted;

	private boolean enterpriseLicenseUserLimitExceededAlerted;
	
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
	
	public boolean isTrialEnterpriseLicenseExpireInOneWeekAlerted() {
		return trialEnterpriseLicenseExpireInOneWeekAlerted;
	}

	public void setTrialEnterpriseLicenseExpireInOneWeekAlerted(boolean trialEnterpriseLicenseExpireInOneWeekAlerted) {
		this.trialEnterpriseLicenseExpireInOneWeekAlerted = trialEnterpriseLicenseExpireInOneWeekAlerted;
	}

	public boolean isTrialEnterpriseLicenseExpiredAlerted() {
		return trialEnterpriseLicenseExpiredAlerted;
	}

	public void setTrialEnterpriseLicenseExpiredAlerted(boolean trialEnterpriseLicenseExpiredAlerted) {
		this.trialEnterpriseLicenseExpiredAlerted = trialEnterpriseLicenseExpiredAlerted;
	}

	public boolean isEnterpriseLicenseExpireInOneMonthAlerted() {
		return enterpriseLicenseExpireInOneMonthAlerted;
	}

	public void setEnterpriseLicenseExpireInOneMonthAlerted(boolean enterpriseLicenseExpireInOneMonthAlerted) {
		this.enterpriseLicenseExpireInOneMonthAlerted = enterpriseLicenseExpireInOneMonthAlerted;
	}

	public boolean isEnterpriseLicenseExpireInOneWeekAlerted() {
		return enterpriseLicenseExpireInOneWeekAlerted;
	}

	public void setEnterpriseLicenseExpireInOneWeekAlerted(boolean enterpriseLicenseExpireInOneWeekAlerted) {
		this.enterpriseLicenseExpireInOneWeekAlerted = enterpriseLicenseExpireInOneWeekAlerted;
	}

	public boolean isEnterpriseLicenseExpiredAlerted() {
		return enterpriseLicenseExpiredAlerted;
	}

	public void setEnterpriseLicenseExpiredAlerted(boolean enterpriseLicenseExpiredAlerted) {
		this.enterpriseLicenseExpiredAlerted = enterpriseLicenseExpiredAlerted;
	}

	public boolean isEnterpriseLicenseUserLimitApproachingAlerted() {
		return enterpriseLicenseUserLimitApproachingAlerted;
	}

	public void setEnterpriseLicenseUserLimitApproachingAlerted(boolean enterpriseLicenseUserLimitApproachingAlerted) {
		this.enterpriseLicenseUserLimitApproachingAlerted = enterpriseLicenseUserLimitApproachingAlerted;
	}

	public boolean isEnterpriseLicenseUserLimitExceededAlerted() {
		return enterpriseLicenseUserLimitExceededAlerted;
	}

	public void setEnterpriseLicenseUserLimitExceededAlerted(boolean enterpriseLicenseUserLimitExceededAlerted) {
		this.enterpriseLicenseUserLimitExceededAlerted = enterpriseLicenseUserLimitExceededAlerted;
	}
	
	public void setAlerted(boolean alerted) {
		trialEnterpriseLicenseExpireInOneWeekAlerted = alerted;
		trialEnterpriseLicenseExpiredAlerted = alerted;
		enterpriseLicenseExpireInOneMonthAlerted = alerted;
		enterpriseLicenseExpireInOneWeekAlerted = alerted;
		enterpriseLicenseExpiredAlerted = alerted;
		enterpriseLicenseUserLimitApproachingAlerted = alerted;
		enterpriseLicenseUserLimitExceededAlerted = alerted;
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
