package io.onedev.server.model.support.administration;

import java.io.Serializable;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable
public class MailSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String smtpHost;
	
	private int smtpPort = 587;
	
	private String smtpUser;
	
	private String smtpPassword;
	
	private String emailAddress;
	
	private ReceiveMailSetting receiveMailSetting;
	
	private boolean enableStartTLS = true;
	
	private int timeout = 60;

	@Editable(order=100, name="SMTP Host")
	@NotEmpty
	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	@Editable(order=200, name="SMTP Port")
	public int getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	@Editable(order=300, name="SMTP User", description=
		"Optionally specify user name here if the SMTP host needs authentication"
		)
	public String getSmtpUser() {
		return smtpUser;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	@Editable(order=400, name="SMTP Password", description=
		"Optionally specify password here if the SMTP host needs authentication"
		)
	@Password(autoComplete="new-password")
	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	@Editable(order=410, name="System Email Address", description="This email address will be used as sender "
			+ "address for various notifications. Its inbox will also be checked if <tt>Check Incoming Email</tt>"
			+ "option is enabled below")
	@NotEmpty
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	@Editable(order=450, name="Check Incoming Email", description="Enable this to post issue and pull request comments via email<br>"
			+ "<b class='text-danger'>NOTE:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Sub addressing</a> "
			+ "needs to be enabled for system email address, as OneDev uses it to track issue and pull request contexts")
	public ReceiveMailSetting getReceiveMailSetting() {
		return receiveMailSetting;
	}

	public void setReceiveMailSetting(ReceiveMailSetting receiveMailSetting) {
		this.receiveMailSetting = receiveMailSetting;
	}

	@Editable(order=550, name="Enable SSL/TLS", description="Whether or not to enable SSL/TLS "
			+ "when interacting with mail server")
	public boolean isEnableStartTLS() {
		return enableStartTLS;
	}

	public void setEnableStartTLS(boolean enableStartTLS) {
		this.enableStartTLS = enableStartTLS;
	}

	@Editable(order=600, description="Specify timeout in seconds when communicating with mail server")
	@Min(value=10, message="This value should not be less than 10")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
}
