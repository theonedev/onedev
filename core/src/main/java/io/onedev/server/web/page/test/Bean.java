package io.onedev.server.web.page.test;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.editable.annotation.Color;
import io.onedev.server.util.editable.annotation.Editable;

@Editable
public class Bean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String color;
	
	@Editable(order=100)
	@Color
	@NotEmpty
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

}
