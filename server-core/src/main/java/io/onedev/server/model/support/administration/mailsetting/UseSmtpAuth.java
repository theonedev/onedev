package io.onedev.server.model.support.administration.mailsetting;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=100, name="Same as SMTP")
public class UseSmtpAuth implements ImapAuth {
	
	private static final long serialVersionUID = 1L;

	@Override
	public String getUserName(OtherMailSetting otherMailSetting) {
		return otherMailSetting.getSmtpUser();
	}

	@Override
	public String getPassword(OtherMailSetting otherMailSetting) {
		return otherMailSetting.getSmtpPassword();
	}

}
