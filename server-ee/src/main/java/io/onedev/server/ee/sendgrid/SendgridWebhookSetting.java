package io.onedev.server.ee.sendgrid;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Editable
public class SendgridWebhookSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String secret;
	
	private boolean monitorSystemAddressOnly;
	
	@Editable(order=100, description = "" +
			"Specify at least 10 alphanumeric chars to be used as secret, and then add an inbound parse entry at SendGrid side:" +
			"<ul>" +
			"<li>" +
			"<code>Destination URL</code> should be set to <i>&lt;OneDev root url&gt;/~sendgrid/&lt;secret&gt;</i>, for instance, " +
			"<i>https://onedev.example.com/~sendgrid/1234567890</i>. Note that in production environment, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https should be enabled</a> " +
			"to protect the secret" +
			"</li>" +
			"<li><code>Receiving domain</code> should be the same as domain part of system email address specified above</li>" +
			"<li>Option <code>POST the raw, full MIME message</code> is enabled</li>" +
			"</ul>")
	@Password
	@NotEmpty
	@Pattern(regexp = "[a-zA-Z0-9]{10,}", message = "At least 10 alphanumeric chars expected")
	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	@Editable(order=200, description = "Check this to only monitor system address above for incoming " +
			"email processing; if not checked, all emails in the inbox will be processed")
	public boolean isMonitorSystemAddressOnly() {
		return monitorSystemAddressOnly;
	}

	public void setMonitorSystemAddressOnly(boolean monitorSystemAddressOnly) {
		this.monitorSystemAddressOnly = monitorSystemAddressOnly;
	}
	
}
