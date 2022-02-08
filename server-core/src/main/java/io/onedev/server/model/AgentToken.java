package io.onedev.server.model;

import static io.onedev.server.model.AgentToken.PROP_VALUE;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(indexes={@Index(columnList=PROP_VALUE)})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class AgentToken extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;

	public static final String PROP_VALUE = "value";
	
	@OneToOne(mappedBy="token", fetch=FetchType.LAZY)
	private Agent agent;
	
	@Column(nullable=false, unique=true)
	private String value;
	
	@Nullable
	public Agent getAgent() {
		return agent;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
