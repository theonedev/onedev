package io.onedev.server.model.support.administration.mailservice;

import io.onedev.server.annotation.Editable;

import java.util.Properties;

@Editable(order=200, name="Explicit SSL (StartTLS)")
public class ImapExplicitSsl extends ImapWithSsl {

	private static final long serialVersionUID = 1L;
	
	private int port = 143;

	@Editable
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void configure(Properties properties) {
		super.configure(properties);
		properties.setProperty("mail.imap.port", String.valueOf(port));
		properties.setProperty("mail.imap.starttls.enable", "true");
		properties.setProperty("mail.imap.starttls.required", "false");
	}
}
