package io.onedev.server.model.support.administration.mailsetting;

import java.io.Serializable;

import javax.validation.constraints.Min;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class InboxPollSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int pollInterval = 60;
	
	@Editable(order=10000, description="Specify incoming email poll interval in seconds")
	@Min(value=10, message="This value should not be less than 10")
	public int getPollInterval() {
		return pollInterval;
	}

	public void setPollInterval(int pollInterval) {
		this.pollInterval = pollInterval;
	}
	
}