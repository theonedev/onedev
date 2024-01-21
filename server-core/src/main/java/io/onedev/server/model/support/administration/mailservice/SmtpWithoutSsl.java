package io.onedev.server.model.support.administration.mailservice;

import io.onedev.server.annotation.Editable;

import java.util.Properties;

@Editable(order=300, name="No SSL")
public class SmtpWithoutSsl implements SmtpSslSetting {

	private static final long serialVersionUID = 1L;
	
	private int port = 25;

	@Editable
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void configure(Properties properties) {
		properties.setProperty("mail.smtp.port", String.valueOf(port));
		properties.setProperty("mail.smtp.localhost", "localhost.localdomain");
	}
	
}
