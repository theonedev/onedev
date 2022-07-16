package io.onedev.server.mail;

import java.io.Serializable;

public class MailSendSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String smtpHost;
	
	private final int smtpPort;
	
	private final String smtpUser;
	
	private final MailCredential smtpCredential;
	
	private final String senderAddress;
	
	private final boolean enableStartTLS;
	
	private final int timeout;

	public MailSendSetting(String smtpHost, int smtpPort, String smtpUser, MailCredential smtpCredential,
			String senderAddress, boolean enableStartTLS, int timeout) {
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
		this.smtpUser = smtpUser;
		this.smtpCredential = smtpCredential;
		this.senderAddress = senderAddress;
		this.enableStartTLS = enableStartTLS;
		this.timeout = timeout;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public int getSmtpPort() {
		return smtpPort;
	}

	public String getSmtpUser() {
		return smtpUser;
	}

	public MailCredential getSmtpCredential() {
		return smtpCredential;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public boolean isEnableStartTLS() {
		return enableStartTLS;
	}

	public int getTimeout() {
		return timeout;
	}
	
}
