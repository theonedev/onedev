package io.onedev.server.model.support.administration;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable
public class MailSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String smtpHost;
	
	private int smtpPort = 587;
	
	private boolean enableStartTLS = true;
	
	private boolean sendAsHtml = true;
	
	private String smtpUser;
	
	private String smtpPassword;
	
	private String senderAddress;
	
	private int timeout = 60;

	@Editable(order=100, name="SMTP Host", description=
		"Specify the SMTP mail host used by OneDev to send email."
		)
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

	@Editable(order=250, name="Enable STARTTLS", description="Whether or not to enable STARTTLS on above port")
	public boolean isEnableStartTLS() {
		return enableStartTLS;
	}

	public void setEnableStartTLS(boolean enableStartTLS) {
		this.enableStartTLS = enableStartTLS;
	}

	@Editable(order=260, name="Send as Html", description=
			"If checked, mail will be sent in html format. Otherwise in plain text format")
	public boolean isSendAsHtml() {
		return sendAsHtml;
	}

	public void setSendAsHtml(boolean sendAsHtml) {
		this.sendAsHtml = sendAsHtml;
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

	@Editable(order=500, description=
		"This property is optional. If specified, OneDev will use this email " +
		"as the sender address when sending out emails. Otherwise, the sender " +
		"address will be <b>onedev@&lt;hostname&gt;</b>, where &lt;hostname&gt; " +
		"is the host name of OneDev server."
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
