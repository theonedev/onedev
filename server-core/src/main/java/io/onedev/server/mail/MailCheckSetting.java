package io.onedev.server.mail;

import io.onedev.server.model.support.administration.mailsetting.ImapSslSetting;

import java.io.Serializable;

public class MailCheckSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String imapHost;
	
	private final ImapSslSetting sslSetting;
	
	private final String imapUser;
	
	private final MailCredential imapCredential;
	
	private final String checkAddress;
	
	private final int pollInterval;
	
	private final int timeout;

	public MailCheckSetting(String imapHost, ImapSslSetting sslSetting, String imapUser, 
							MailCredential imapCredential, String checkAddress, 
							int pollInterval, int timeout) {
		this.imapHost = imapHost;
		this.sslSetting = sslSetting;
		this.imapUser = imapUser;
		this.imapCredential = imapCredential;
		this.checkAddress = checkAddress;
		this.pollInterval = pollInterval;
		this.timeout = timeout;
	}

	public String getImapHost() {
		return imapHost;
	}

	public ImapSslSetting getSslSetting() {
		return sslSetting;
	}

	public String getImapUser() {
		return imapUser;
	}

	public MailCredential getImapCredential() {
		return imapCredential;
	}

	public String getCheckAddress() {
		return checkAddress;
	}

	public int getPollInterval() {
		return pollInterval;
	}

	public int getTimeout() {
		return timeout;
	}
	
}
