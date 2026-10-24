package io.onedev.server.mail;

import java.io.Serial;

public class BasicAuthPassword implements MailCredential {

	@Serial
	private static final long serialVersionUID = 1L;

	private final String value;
	
	public BasicAuthPassword(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

}
