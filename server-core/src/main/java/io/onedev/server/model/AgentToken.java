package io.onedev.server.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import java.util.UUID;

import static io.onedev.server.model.AgentToken.PROP_VALUE;

@Entity
@Table(indexes={@Index(columnList=PROP_VALUE)})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class AgentToken extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;

	public static final String PROP_VALUE = "value";
	
	@Column(nullable=false, unique=true)
	private String value = UUID.randomUUID().toString();

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
