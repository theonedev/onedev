package io.onedev.server.model.support.administration.mailsetting;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable(order=200, name="Specify User Name and Password")
public class SeparateImapAuth implements ImapAuth {
	
	private static final long serialVersionUID = 1L;

	private String userName;
	
	private String password;
	
	@Editable(order=500, name="IMAP User", description="Specify IMAP user name.<br>"
			+ "<b class='text-danger'>NOTE: </b> This account should be able to receive emails sent to system "
			+ "email address specified above")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=600, name="IMAP Password")
	@Password(autoComplete="new-password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getUserName(OtherMailSetting otherMailSetting) {
		return getUserName();
	}

	@Override
	public String getPassword(OtherMailSetting otherMailSetting) {
		return getPassword();
	}

}
