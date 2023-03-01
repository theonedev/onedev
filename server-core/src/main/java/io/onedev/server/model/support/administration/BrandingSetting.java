package io.onedev.server.model.support.administration;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;

@Editable
public class BrandingSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name = "OneDev";
	
	@Editable(order=100, description="Specify brand name which will be displayed at left top of the screen")
	@NotEmpty
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
