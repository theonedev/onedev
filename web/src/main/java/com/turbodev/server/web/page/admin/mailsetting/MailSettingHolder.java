package com.turbodev.server.web.page.admin.mailsetting;

import java.io.Serializable;

import com.turbodev.server.model.support.setting.MailSetting;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable
public class MailSettingHolder implements Serializable {

	private static final long serialVersionUID = 1L;

	private MailSetting mailSetting;

	@Editable(name="Enable")
	public MailSetting getMailSetting() {
		return mailSetting;
	}

	public void setMailSetting(MailSetting mailSetting) {
		this.mailSetting = mailSetting;
	}
	
}
