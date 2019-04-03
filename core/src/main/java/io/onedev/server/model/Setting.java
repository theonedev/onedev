package io.onedev.server.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

/**
 * This entity stores object in serialized form, with one entity 
 * representing one object. 
 *
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@DynamicUpdate
public class Setting extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public enum Key {SYSTEM, MAIL, BACKUP, SECURITY, AUTHENTICATOR, ISSUE, JOB_EXECUTORS};
	
	/*
	 * Optimistic lock is necessary to ensure database integrity when update 
	 * information such as authenticator default group names 
	 */
	@Version
	private long version;
	
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
