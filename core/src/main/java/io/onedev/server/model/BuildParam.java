package io.onedev.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
		indexes={@Index(columnList="o_build_id"), @Index(columnList="name"), @Index(columnList="value")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_build_id", "name", "value"})})
public class BuildParam extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String FIELD_ATTR_NAME = "name";
	
	public static final String FIELD_ATTR_VALUE = "value";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build build;
	
	@Column(nullable=false)
	private String name;

	@Column(nullable=false)
	private String value;

	public Build getBuild() {
		return build;
	}

	public void setBuild(Build build) {
		this.build = build;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
