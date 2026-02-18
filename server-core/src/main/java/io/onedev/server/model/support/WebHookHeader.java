package io.onedev.server.model.support;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;

@Editable
public class WebHookHeader implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String value;

	@Editable(order=100, name="Header Name")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, name="Header Value")
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
