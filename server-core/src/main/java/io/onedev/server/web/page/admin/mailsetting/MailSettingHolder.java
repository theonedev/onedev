package io.onedev.server.web.page.admin.mailsetting;

import java.io.Serializable;

import io.onedev.server.model.support.administration.mailsetting.MailSetting;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class MailSettingHolder implements Serializable {

	private static final long serialVersionUID = 1L;

	private MailSetting mailSetting;

	@Editable(name="Provider", placeholder="None")
	public MailSetting getMailSetting() {
		return mailSetting;
	}

	public void setMailSetting(MailSetting mailSetting) {
		this.mailSetting = mailSetting;
	}
	
}
