package io.onedev.server.model.support.issue;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Color;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class StateSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String description;
	
	private String color = "#0d87e9";
	
	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200)
	@NameOfEmptyValue("No description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Editable(order=400, description="Specify color of the state for displaying purpose")
	@Color
	@NotEmpty(message="choose a color for this state")
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

}
