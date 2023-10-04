package io.onedev.server.model.support.administration.mailservice;

import io.onedev.server.annotation.Editable;

import java.util.Properties;

@Editable(order=200, name="Implicit SSL")
public class SmtpImplicitSsl extends SmtpWithSsl {

	private static final long serialVersionUID = 1L;
	
	private int port = 465;

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
		properties.setProperty("mail.smtp.port", String.valueOf(port));
		properties.setProperty("mail.smtp.ssl.enable", "true");
	}
}
