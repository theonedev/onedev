package com.pmease.gitplex.core.setting;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Password;

@Editable
public class MailSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String smtpHost;
	
	private int smtpPort = 25;
	
	private int sslSmtpPort = 465;
	
	private boolean smtpOverSSL;
	
	private String smtpUser;
	
	private String smtpPassword;
	
	private boolean enableStartTLS;
	
	private String senderAddress;
	
	private String replyAddress;
	
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

	@Editable(order=210, name="SSL SMTP Port", description=
			"Specify SSL port number for the above SMTP host. This port number "
			+ "will be used if the option 'Use SMTP over SSL' is checked."
			)
	public int getSslSmtpPort() {
		return sslSmtpPort;
	}

	public void setSslSmtpPort(int sslSmtpPort) {
		this.sslSmtpPort = sslSmtpPort;
	}

	@Editable(order=250, name="Use SMTP over SSL?", description=
    	"Whether or not send email using SMTP over SSL. If SSL is enabled, and a " +
    	"<b>SSLHandshakeException</b> is thrown with message <b>unable to find " +
    	"valid certification path to requested target</b> while sending the email, " +
    	"please make sure public key of your SMTP server is trusted by GitPlex."
    	)
	public boolean isSmtpOverSSL() {
		return smtpOverSSL;
	}

	public void setSmtpOverSSL(boolean smtpOverSSL) {
		this.smtpOverSSL = smtpOverSSL;
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
	@Password
	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	@Editable(order=450, name="Enable Start TLS?", description=
			"Whether the STARTTLS command used to switch to an encrypted connection for authentication."
			)
	public boolean isEnableStartTLS() {
		return enableStartTLS;
	}

	public void setEnableStartTLS(boolean enableStartTLS) {
		this.enableStartTLS = enableStartTLS;
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

	@Editable(order=500, description=
			"This property is optional. If specified, GitPlex will use this email " +
			"as the reply-to address when user replies to the notification email. Otherwise, " +
			"the sender address will be used."
			)
	public String getReplyAddress() {
		return replyAddress;
	}

	public void setReplyAddress(String replyAddress) {
		this.replyAddress = replyAddress;
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
