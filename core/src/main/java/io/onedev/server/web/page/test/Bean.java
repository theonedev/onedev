package io.onedev.server.web.page.test;

import java.io.Serializable;

import io.onedev.server.util.editable.annotation.Color;
import io.onedev.server.util.editable.annotation.Editable;

@Editable
public class Bean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String color;
	
	private String state;

	@Editable(order=100)
	@Color
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@Editable(order=200)
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
}
