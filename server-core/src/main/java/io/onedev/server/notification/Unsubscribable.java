package io.onedev.server.notification;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

public class Unsubscribable implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String emailAddress;
	
	public Unsubscribable(@Nullable String emailAddress) {
		this.emailAddress = emailAddress;
	}

	@Nullable
	public String getEmailAddress() {
		return emailAddress;
	}
	
}
