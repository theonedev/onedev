package io.onedev.server.web.page.admin.mailservice;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.administration.mailservice.MailConnector;

import java.io.Serializable;

@Editable
public class MailConnectorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private MailConnector mailConnector;

	@Editable(placeholder="No mail service")
	public MailConnector getMailConnector() {
		return mailConnector;
	}

	public void setMailConnector(MailConnector mailConnector) {
		this.mailConnector = mailConnector;
	}

}
