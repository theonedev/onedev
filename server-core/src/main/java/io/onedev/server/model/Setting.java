package io.onedev.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.io.Serializable;

/**
 * This entity stores object in serialized form, with one entity 
 * representing one object. 
 *
 */
@Entity
public class Setting extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_KEY = "key";
	
	public enum Key {SYSTEM, MAIL_SERVICE, BACKUP, SECURITY, AUTHENTICATOR, ISSUE, JOB_EXECUTORS, 
		GROOVY_SCRIPTS, PULL_REQUEST, BUILD, PROJECT, SSH, GPG, SSO_CONNECTORS,
		EMAIL_TEMPLATES, CONTRIBUTED_SETTINGS, SERVICE_DESK_SETTING, 
		AGENT, PERFORMANCE, BRANDING, CLUSTER_SETTING, SUBSCRIPTION_DATA, ALERT, 
		SYSTEM_UUID
	};
	
	@Column(nullable=false, unique=true)
	private Key key;
	
	/* 
	 * This field is allowed to be null to indicate particular setting is not 
	 * available (the record will always be available after interactive setup
	 * to indicate that the setting has been prompted (although the user may
	 * skipped the setting), so we can not use existence of record to indicate
	 * a null setting.
	 */
	@Lob
	@Column(length=65535)
	private Serializable value;

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Serializable getValue() {
		return value;
	}

	public void setValue(Serializable value) {
		this.value = value;
	}

}
