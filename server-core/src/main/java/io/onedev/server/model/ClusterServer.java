package io.onedev.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ClusterServer extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ADDRESS = "address";
	
	@Column(nullable=false, unique=true)
	private String address;
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
}
