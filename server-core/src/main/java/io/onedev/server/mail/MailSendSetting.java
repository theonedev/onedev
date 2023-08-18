package io.onedev.server.mail;

import io.onedev.server.model.support.administration.mailsetting.SmtpSslSetting;

import java.io.Serializable;

public class MailSendSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String smtpHost;
	
	private final SmtpSslSetting sslSetting;
	
	private final String smtpUser;
	
	private final MailCredential smtpCredential;
	
	private final String senderAddress;
	
	private final int timeout;

	public MailSendSetting(String smtpHost, SmtpSslSetting sslSetting, String smtpUser, 
						   MailCredential smtpCredential, String senderAddress, int timeout) {
		this.smtpHost = smtpHost;
		this.sslSetting = sslSetting;
		this.smtpUser = smtpUser;
		this.smtpCredential = smtpCredential;
		this.senderAddress = senderAddress;
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

	public String getSenderAddress() {
		return senderAddress;
	}

	public int getTimeout() {
		return timeout;
	}
	
}
