package io.onedev.server.plugin.mail.gmail;

import io.onedev.server.annotation.Editable;

import javax.validation.constraints.Min;
import java.io.Serializable;

@Editable
public class InboxPollSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int pollInterval = 60;
	
	@Editable(order=100, description="Specify incoming email poll interval in seconds")
	@Min(value=10, message="This value should not be less than 10")
	public int getPollInterval() {
		return pollInterval;
	}

	public void setPollInterval(int pollInterval) {
		this.pollInterval = pollInterval;
	}
	
}