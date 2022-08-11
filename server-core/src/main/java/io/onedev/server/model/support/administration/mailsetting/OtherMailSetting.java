package io.onedev.server.model.support.administration.mailsetting;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.mail.BasicAuthPassword;
import io.onedev.server.mail.MailCheckSetting;
import io.onedev.server.mail.MailCredential;
import io.onedev.server.mail.MailSendSetting;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable(order=10000, name="Other")
public class OtherMailSetting extends MailSetting {
	
	private static final long serialVersionUID = 1L;

	private String smtpHost;
	
	private int smtpPort = 587;
	
	private String smtpUser;
	
	private String smtpPassword;
	
	private String emailAddress;
	
	private OtherInboxPollSetting otherInboxPollSetting;
	
	private boolean enableStartTLS = true;

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

	@Editable(order=300, name="SMTP User")
	public String getSmtpUser() {
		return smtpUser;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	@Editable(order=400, name="SMTP Password")
	@Password(autoComplete="new-password")
	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	@Editable(order=410, description="This address will be used as sender address of various notifications. "
			+ "Emails targeting this address and its sub addressing in the IMAP inbox will also be checked if "
			+ "<code>Check Incoming Email</code> option is enabled below")
	@Email
	@NotEmpty
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	@Editable(order=430, name="Enable SMTP StartTLS", description="Whether or not to enable StartTLS "
			+ "when connect to SMTP server")
	public boolean isEnableStartTLS() {
		return enableStartTLS;
	}

	public void setEnableStartTLS(boolean enableStartTLS) {
		this.enableStartTLS = enableStartTLS;
	}

	@Editable(order=450, name="Check Incoming Email", description="Enable this to post issue and pull request comments via email. "
			+ "<b class='text-danger'>NOTE:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Sub addressing</a> "
			+ "needs to be enabled for above email address, as OneDev uses it to track issue and pull request contexts")
	public OtherInboxPollSetting getOtherInboxPollSetting() {
		return otherInboxPollSetting;
	}

	public void setOtherInboxPollSetting(OtherInboxPollSetting otherInboxPollSetting) {
		this.otherInboxPollSetting = otherInboxPollSetting;
	}

	@Override
	public MailSendSetting getSendSetting() {
		MailCredential smtpCredential;
		if (smtpPassword != null)
			smtpCredential = new BasicAuthPassword(smtpPassword);
		else
			smtpCredential = null;
		return new MailSendSetting(smtpHost, smtpPort, smtpUser, smtpCredential, emailAddress, enableStartTLS, getTimeout());
	}

	@Override
	public MailCheckSetting getCheckSetting() {
		if (otherInboxPollSetting != null) {
			String imapUser = otherInboxPollSetting.getImapUser();
			MailCredential imapCredential = new BasicAuthPassword(otherInboxPollSetting.getImapPassword());
			return new MailCheckSetting(otherInboxPollSetting.getImapHost(), otherInboxPollSetting.getImapPort(), 
					imapUser, imapCredential, emailAddress, otherInboxPollSetting.isEnableSSL(), 
					otherInboxPollSetting.getPollInterval(), getTimeout());
		} else {
			return null;
		}
	}

}
