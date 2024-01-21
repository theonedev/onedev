package io.onedev.server.mail;

import io.onedev.server.model.support.administration.mailservice.SmtpSslSetting;

import java.io.Serializable;

public class SmtpSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String smtpHost;
	
	private final SmtpSslSetting sslSetting;
	
	private final String smtpUser;
	
	private final MailCredential smtpCredential;
	
	private final int timeout;

	public SmtpSetting(String smtpHost, SmtpSslSetting sslSetting, String smtpUser,
					   MailCredential smtpCredential, int timeout) {
		this.smtpHost = smtpHost;
		this.sslSetting = sslSetting;
		this.smtpUser = smtpUser;
		this.smtpCredential = smtpCredential;
		this.timeout = timeout;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public SmtpSslSetting getSslSetting() {
		return sslSetting;
	}

	public String getSmtpUser() {
		return smtpUser;
	}

	public MailCredential getSmtpCredential() {
		return smtpCredential;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
}
