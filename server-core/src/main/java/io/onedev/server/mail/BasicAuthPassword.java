package io.onedev.server.mail;

public class BasicAuthPassword implements MailCredential {

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
