package io.onedev.server.mail;

import java.io.Serializable;

import javax.annotation.Nullable;

public class MailCheckSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String imapHost;
	
	private final int imapPort;
	
	private final String imapUser;
	
	private final MailCredential imapCredential;
	
	private final String checkAddress;
	
	private final boolean enableSSL;
	
	private final int pollInterval;
	
	private final int timeout;

	public MailCheckSetting(String imapHost, int imapPort, @Nullable String imapUser, @Nullable MailCredential imapCredential,
			String checkAddress, boolean enableSSL, int pollInterval, int timeout) {
		this.imapHost = imapHost;
		this.imapPort = imapPort;
		this.imapUser = imapUser;
		this.imapCredential = imapCredential;
		this.checkAddress = checkAddress;
		this.enableSSL = enableSSL;
		this.pollInterval = pollInterval;
		this.timeout = timeout;
	}

	public String getImapHost() {
		return imapHost;
	}

	public int getImapPort() {
		return imapPort;
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

	public boolean isEnableSSL() {
		return enableSSL;
	}

	public int getPollInterval() {
		return pollInterval;
	}

	public int getTimeout() {
		return timeout;
	}
	
}
