package io.onedev.server.model;

import static io.onedev.server.model.BuildParam.PROP_NAME;
import static io.onedev.server.model.BuildParam.PROP_VALUE;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.onedev.server.buildspecmodel.inputspec.textinput.TextInput;

/**
 * @author robin
 *
 */
@Entity
@Table(
		indexes={@Index(columnList="build_id"), @Index(columnList=PROP_NAME), @Index(columnList=PROP_VALUE)},
		uniqueConstraints={@UniqueConstraint(columnNames={"build_id", PROP_NAME, PROP_VALUE})})
public class BuildParam extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_BUILD = "build";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_VALUE = "value";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build build;
	
	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false)
	private String type;
	
	@Column(length=TextInput.MAX_LEN)
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
