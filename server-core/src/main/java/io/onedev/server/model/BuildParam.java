package io.onedev.server.model;

import static io.onedev.server.model.BuildParam.PROP_NAME;
import static io.onedev.server.model.BuildParam.PROP_VALUE;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonView;

import io.onedev.server.util.jackson.DefaultView;

/**
 * @author robin
 *
 */
@Entity
@Table(
		indexes={@Index(columnList="o_build_id"), @Index(columnList=PROP_NAME), @Index(columnList=PROP_VALUE)}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_build_id", PROP_NAME, PROP_VALUE})})
public class BuildParam extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_NAME = "name";
	
	public static final String PROP_VALUE = "value";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build build;
	
	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false)
	private String type;
	
	@JsonView(DefaultView.class)
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Nullable
	public String getValue() {
		return value;
	}

	public void setValue(@Nullable String value) {
		this.value = value;
	}

}
