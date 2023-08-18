package io.onedev.server.model.support.administration.mailsetting;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Editable
public class OtherInboxPollSetting extends InboxPollSetting {

	private static final long serialVersionUID = 1L;
	
	private String imapHost;
	
	private ImapSslSetting sslSetting = new ImapImplicitSsl();
	
	private String imapUser;
	
	private String imapPassword;
	
	@Editable(order=100, name="IMAP Host")
	@NotEmpty
	public String getImapHost() {
		return imapHost;
	}

	public void setImapHost(String imapHost) {
		this.imapHost = imapHost;
	}

	@Editable(order=200)
	@NotNull
	public ImapSslSetting getSslSetting() {
		return sslSetting;
	}

	public void setSslSetting(ImapSslSetting sslSetting) {
		this.sslSetting = sslSetting;
	}

	@Editable(order=300, name="IMAP User", description="Specify IMAP user name.<br>"
			+ "<b class='text-danger'>NOTE: </b> This account should be able to receive emails sent to system "
			+ "email address specified above")
	@NotEmpty
	public String getImapUser() {
		return imapUser;
	}

	public void setImapUser(String imapUser) {
		this.imapUser = imapUser;
	}

	@Editable(order=400, name="IMAP Password")
	@Password(autoComplete="new-password")
	@NotEmpty
	public String getImapPassword() {
		return imapPassword;
	}

	public void setImapPassword(String imapPassword) {
		this.imapPassword = imapPassword;
	}

}