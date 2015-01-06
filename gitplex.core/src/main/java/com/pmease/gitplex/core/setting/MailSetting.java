package com.pmease.gitplex.core.setting;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;

@Editable
public class MailSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String smtpHost;
	
	private int smtpPort = 25;
	
	private String smtpUser;
	
	private String smtpPassword;
	
	private String senderAddress;

	@Editable(order=100)
	@NotEmpty
	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	@Editable(order=200)
	public int getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	@Editable(order=300)
	public String getSmtpUser() {
		return smtpUser;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	@Editable(order=400)
	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	@Editable(order=500)
	public String getSenderAddress() {
		return senderAddress;
	}

	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

}
