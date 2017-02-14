package com.gitplex.server.web.page.admin;

import java.io.Serializable;

import com.gitplex.commons.wicket.editable.annotation.Editable;
import com.gitplex.server.core.setting.MailSetting;

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
