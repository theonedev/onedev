package io.onedev.server.model.support.inputspec.choiceinput.choiceprovider;

import java.io.Serializable;
import java.util.UUID;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Color;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(name="Value")
public class Choice implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uuid = UUID.randomUUID().toString();
	
	private String value;
	
	private String color;

	@Editable
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Editable(order=100)
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Editable(order=200)
	@Color
	@NameOfEmptyValue("No color")
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
}
