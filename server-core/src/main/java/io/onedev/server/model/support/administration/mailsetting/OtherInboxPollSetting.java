package io.onedev.server.model.support.administration.mailsetting;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class OtherInboxPollSetting extends InboxPollSetting {

	private static final long serialVersionUID = 1L;
	
	private String imapHost;
	
	private int imapPort = 993;
	
	private ImapAuth imapAuth = new UseSmtpAuth();
	
	private boolean enableSSL = true;
	
	@Editable(order=100, name="IMAP Host")
	@NotEmpty
	public String getImapHost() {
		return imapHost;
	}

	public void setImapHost(String imapHost) {
		this.imapHost = imapHost;
	}

	@Editable(order=200, name="IMAP Port")
	public int getImapPort() {
		return imapPort;
	}

	public void setImapPort(int imapPort) {
		this.imapPort = imapPort;
	}

	@Editable(order=300, name="Authentication")
	@NotNull
	public ImapAuth getImapAuth() {
		return imapAuth;
	}

	public void setImapAuth(ImapAuth imapAuth) {
		this.imapAuth = imapAuth;
	}

	@Editable(order=700, name="Enable IMAP SSL", description="Whether or not to enable SSL when connect to IMAP server")
	public boolean isEnableSSL() {
		return enableSSL;
	}

	public void setEnableSSL(boolean enableSSL) {
		this.enableSSL = enableSSL;
	}

}