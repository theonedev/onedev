package io.onedev.server.model.support.administration;

import java.io.Serializable;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable
public class ReceiveMailSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String imapHost;
	
	private int imapPort = 993;
	
	private String imapUser;
	
	private String imapPassword;
	
	private boolean enableSSL = true;
	
	private int pollInterval = 60;
	
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

	@Editable(order=500, name="IMAP User", description="Specify IMAP user name.<br>"
			+ "<b class='text-danger'>NOTE: </b> This account should be able to receive emails sent to system "
			+ "email address specified above")
	@NotEmpty
	public String getImapUser() {
		return imapUser;
	}

	public void setImapUser(String imapUser) {
		this.imapUser = imapUser;
	}

	@Editable(order=600, name="IMAP Password")
	@Password(autoComplete="new-password")
	@NotEmpty
	public String getImapPassword() {
		return imapPassword;
	}

	public void setImapPassword(String imapPassword) {
		this.imapPassword = imapPassword;
	}

	@Editable(order=700, name="Enable IMAP SSL", description="Whether or not to enable SSL when connect to IMAP server")
	public boolean isEnableSSL() {
		return enableSSL;
	}

	public void setEnableSSL(boolean enableSSL) {
		this.enableSSL = enableSSL;
	}

	@Editable(order=800, description="Specify incoming email poll interval in seconds")
	@Min(value=10, message="This value should not be less than 10")
	public int getPollInterval() {
		return pollInterval;
	}

	public void setPollInterval(int pollInterval) {
		this.pollInterval = pollInterval;
	}
	
}