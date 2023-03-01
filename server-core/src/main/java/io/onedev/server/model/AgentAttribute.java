package io.onedev.server.model;

import static io.onedev.server.model.AgentAttribute.PROP_NAME;
import static io.onedev.server.model.AgentAttribute.PROP_VALUE;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.AttributeName;
import io.onedev.server.annotation.Editable;

/**
 * @author robin
 *
 */
@Entity
@Table(
		indexes={@Index(columnList="o_agent_id"), @Index(columnList=PROP_NAME), @Index(columnList=PROP_VALUE)}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_agent_id", PROP_NAME})})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class AgentAttribute extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_AGENT = "agent";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_VALUE = "value";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Agent agent;
	
	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false)
	private String value;

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	@Editable(order=100, name="Name")
	@AttributeName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, name="Value")
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
