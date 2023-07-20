package io.onedev.server.model.support.issue;

import io.onedev.server.annotation.Color;
import io.onedev.server.annotation.Editable;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

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

	@Editable(order=200, placeholder="No description")
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
