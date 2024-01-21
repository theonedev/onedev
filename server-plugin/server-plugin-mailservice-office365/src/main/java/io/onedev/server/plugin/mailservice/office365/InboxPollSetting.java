package io.onedev.server.plugin.mailservice.office365;

import io.onedev.server.annotation.Editable;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable
public class InboxPollSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int pollInterval = 60;
	
	private List<String> additionalTargetAddresses = new ArrayList<>();
	
	@Editable(order=100, description="Specify incoming email poll interval in seconds")
	@Min(value=10, message="This value should not be less than 10")
	public int getPollInterval() {
		return pollInterval;
	}

	public void setPollInterval(int pollInterval) {
		this.pollInterval = pollInterval;
	}

	@Editable(order=200, name="Additional Email Addresses to Monitor", placeholder = "Input email address and press ENTER", description = "Emails sent to these " +
			"email addresses will also be processed besides system email address specified above")
	public List<String> getAdditionalTargetAddresses() {
		return additionalTargetAddresses;
	}

	public void setAdditionalTargetAddresses(List<String> additionalTargetAddresses) {
		this.additionalTargetAddresses = additionalTargetAddresses;
	}
	
}