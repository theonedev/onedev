package io.onedev.server.model.support.administration.mailsetting;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;

import io.onedev.server.mail.MailCheckSetting;
import io.onedev.server.mail.MailSendSetting;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class MailSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int timeout = 60;

	@Editable(order=10000, description="Specify timeout in seconds when communicating with mail server")
	@Min(value=10, message="This value should not be less than 10")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public abstract MailSendSetting getSendSetting();
	
	@Nullable
	public abstract MailCheckSetting getCheckSetting();
	
}
