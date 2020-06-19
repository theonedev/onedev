package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.UUID;

import javax.annotation.Nullable;
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
	
	/*
	 * SQL Server treats null as a value when checking unique constraints. So we 
	 * need to populate subject even if no SSO is used to avoid violating unique
	 * constraint on connector and subject combo
	 */
	@Column(name=COLUMN_SUBJECT, nullable=false)
	private String subject = UUID.randomUUID().toString();

	@Nullable
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
