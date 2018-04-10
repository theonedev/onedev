package io.onedev.server.util.inputspec.choiceprovider;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.editable.annotation.Color;
import io.onedev.server.util.editable.annotation.Editable;

@Editable
public class Choice implements Serializable {

	private static final long serialVersionUID = 1L;

	private String value;
	
	private String color = "#FFF";

	@Editable(order=100)
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Editable(order=200, description="Value will be displayed with this color")
	@Color
	@NotEmpty
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
}
