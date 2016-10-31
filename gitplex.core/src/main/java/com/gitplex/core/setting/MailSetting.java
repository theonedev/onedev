package com.gitplex.core.setting;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.commons.wicket.editable.annotation.Editable;
import com.gitplex.commons.wicket.editable.annotation.Password;

@Editable
public class MailSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String smtpHost;
	
	private int smtpPort = 587;
	
	private String smtpUser;
	
	private String smtpPassword;
	
	private String senderAddress;
	
	private int timeout = 300;

	@Editable(order=100, name="SMTP Host", description=
		"Specify the SMTP mail host used by GitPlex to send email."
		)
	@NotEmpty
	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	@Editable(order=200, name="SMTP Port", description=
		"Specify port number for the above SMTP host. This port number "
		+ "will be used if the option 'Use SMTP over SSL' is not checked."
		)
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

	@Editable(order=400, name="SMTP Password", autocomplete="new-password", description=
		"Optionally specify password here if the SMTP host needs authentication"
		)
	@Password
	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	@Editable(order=500, description=
		"This property is optional. If specified, GitPlex will use this email " +
		"as the sender address when sending out emails. Otherwise, the sender " +
		"address will be <b>gitplex@&lt;hostname&gt;</b>, where &lt;hostname&gt; " +
		"is the host name of GitPlex server."
		)
	public String getSenderAddress() {
		return senderAddress;
	}

	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

	@Editable(order=600, description="Specify timeout in seconds when communicating with the SMTP server. " +
			"Use 0 to set an infinite timeout.")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
