package io.onedev.server.model.support;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SsoInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String COLUMN_CONNECTOR = "SSO_CONNECTOR";
	
	public static final String COLUMN_SUBJECT = "SSO_SUBJECT";
	
	public static final String PROP_CONNECTOR = "connector";
	
	public static final String PROP_SUBJECT = "subject";
	
	@Column(name=COLUMN_CONNECTOR)
	private String connector;
	
	@Column(name=COLUMN_SUBJECT)
	private String subject;

	public String getConnector() {
		return connector;
	}

	public void setConnector(String connector) {
		this.connector = connector;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}
