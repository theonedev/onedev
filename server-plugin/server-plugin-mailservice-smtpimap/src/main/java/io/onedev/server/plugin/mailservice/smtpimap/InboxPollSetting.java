package io.onedev.server.plugin.mailservice.smtpimap;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;
import io.onedev.server.model.support.administration.mailservice.ImapSslSetting;
import io.onedev.server.model.support.administration.mailservice.ImapImplicitSsl;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Editable
public class InboxPollSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String imapHost;
	
	private ImapSslSetting sslSetting = new ImapImplicitSsl();
	
	private String imapUser;
	
	private String imapPassword;
	
	private int pollInterval = 60;
	
	private boolean monitorSystemAddressOnly = true;
	
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

	@Editable(order=500, description="Specify incoming email poll interval in seconds")
	@Min(value=10, message="This value should not be less than 10")
	public int getPollInterval() {
		return pollInterval;
	}

	public void setPollInterval(int pollInterval) {
		this.pollInterval = pollInterval;
	}

	@Editable(order=600, description = "Check this to only monitor system address above for incoming " +
			"email processing; if not checked, all emails in the inbox will be processed")
	public boolean isMonitorSystemAddressOnly() {
		return monitorSystemAddressOnly;
	}

	public void setMonitorSystemAddressOnly(boolean monitorSystemAddressOnly) {
		this.monitorSystemAddressOnly = monitorSystemAddressOnly;
	}

}