package io.onedev.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class ClusterCredential extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_VALUE = "value";
	
	@Column(nullable=false)
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
