package io.onedev.server.web.page.admin.mailservice;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.administration.mailservice.MailService;

import java.io.Serializable;

@Editable
public class MailServiceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private MailService mailService;

	@Editable(placeholder="No mail service")
	public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

}
