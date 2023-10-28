package io.onedev.server.plugin.mailservice.gmail;

import io.onedev.server.annotation.Editable;

import javax.validation.constraints.Min;
import java.io.Serializable;

@Editable
public class InboxPollSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int pollInterval = 60;

	private boolean monitorSystemAddressOnly = true;
	
	@Editable(order=100, description="Specify incoming email poll interval in seconds")
	@Min(value=10, message="This value should not be less than 10")
	public int getPollInterval() {
		return pollInterval;
	}

	public void setPollInterval(int pollInterval) {
		this.pollInterval = pollInterval;
	}
	
	@Editable(order=200, description = "Check this to only monitor system address above for incoming " +
			"email processing; if not checked, all emails in the inbox will be processed")
	public boolean isMonitorSystemAddressOnly() {
		return monitorSystemAddressOnly;
	}

	public void setMonitorSystemAddressOnly(boolean monitorSystemAddressOnly) {
		this.monitorSystemAddressOnly = monitorSystemAddressOnly;
	}
}