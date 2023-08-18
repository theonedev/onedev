package io.onedev.server.model.support.administration.mailsetting;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.onedev.server.mail.BasicAuthPassword;
import io.onedev.server.mail.MailCheckSetting;
import io.onedev.server.mail.MailCredential;
import io.onedev.server.mail.MailSendSetting;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;

@Editable(order=10000, name="Other")
public class OtherMailSetting extends MailSetting {
	
	private static final long serialVersionUID = 1L;

	private String smtpHost;
	
	private SmtpSslSetting sslSetting = new SmtpExplicitSsl();
	
	private String smtpUser;
	
	private String smtpPassword;
	
	private String emailAddress;
	
	private OtherInboxPollSetting otherInboxPollSetting;

	@Editable(order=100, name="SMTP Host")
	@NotEmpty
	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	@Editable(order=200, name="SSL Setting")
	@NotNull
	public SmtpSslSetting getSslSetting() {
		return sslSetting;
	}

	public void setSslSetting(SmtpSslSetting sslSetting) {
		this.sslSetting = sslSetting;
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

	@Editable(order=410, name="System Email Address", description="This address will be used as sender address of "
			+ "various notifications. Emails targeting this address and its sub addressing in the IMAP inbox will "
			+ "also be checked if <code>Check Incoming Email</code> option is enabled below")
	@Email
	@NotEmpty
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
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
		return new MailSendSetting(smtpHost, sslSetting, smtpUser, smtpCredential, emailAddress, getTimeout());
	}

	@Override
	public MailCheckSetting getCheckSetting() {
		if (otherInboxPollSetting != null) {
			String imapUser = otherInboxPollSetting.getImapUser();
			MailCredential imapCredential = new BasicAuthPassword(otherInboxPollSetting.getImapPassword());
			return new MailCheckSetting(otherInboxPollSetting.getImapHost(), otherInboxPollSetting.getSslSetting(), 
					imapUser, imapCredential, emailAddress, otherInboxPollSetting.getPollInterval(), getTimeout());
		} else {
			return null;
		}
	}

}
