package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable
public class ReceiveMailSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String imapHost;
	
	private int imapPort = 993;
	
	private String imapUser;
	
	private String imapPassword;
	
	private List<SenderAuthorization> senderAuthorizations = new ArrayList<>();
	
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

	@Editable(order=500, name="IMAP User", description="Specify user name here if the IMAP host needs authentication.<br>"
			+ "<b class='text-danger'>NOTE: </b> Inbox of this account should be able to receive replies to system email address specified above")
	@NotEmpty
	public String getImapUser() {
		return imapUser;
	}

	public void setImapUser(String imapUser) {
		this.imapUser = imapUser;
	}

	@Editable(order=600, name="IMAP Password", description="Specify password here if the IMAP host needs authentication")
	@Password(autoComplete="new-password")
	@NotEmpty
	public String getImapPassword() {
		return imapPassword;
	}

	public void setImapPassword(String imapPassword) {
		this.imapPassword = imapPassword;
	}
	
	@Editable(order=700, description="Only emails from authorized senders will be processed")
	public List<SenderAuthorization> getSenderAuthorizations() {
		return senderAuthorizations;
	}

	public void setSenderAuthorizations(List<SenderAuthorization> senderAuthorizations) {
		this.senderAuthorizations = senderAuthorizations;
	}

	@Nullable
	public SenderAuthorization getSenderAuthorization(String senderAddress) {
		for (SenderAuthorization authorization: senderAuthorizations) {
			String patterns = authorization.getSenderEmails();
			if (patterns == null)
				patterns = "*";
			Matcher matcher = new StringMatcher();
			PatternSet patternSet = PatternSet.parse(patterns);
			if (patternSet.matches(matcher, senderAddress))
				return authorization;
		}
		return null;
	}
	
}