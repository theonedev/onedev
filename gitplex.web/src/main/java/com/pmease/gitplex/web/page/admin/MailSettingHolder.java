package com.pmease.gitplex.web.page.admin;

import java.io.Serializable;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.setting.MailSetting;

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
