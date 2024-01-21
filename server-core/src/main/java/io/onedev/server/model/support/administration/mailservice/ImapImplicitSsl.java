package io.onedev.server.model.support.administration.mailservice;

import io.onedev.server.annotation.Editable;

import java.util.Properties;

@Editable(order=100, name="Implicit SSL")
public class ImapImplicitSsl extends ImapWithSsl {

	private static final long serialVersionUID = 1L;
	
	private int port = 993;

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
		properties.setProperty("mail.imap.ssl.enable", "true");
	}
	
}
