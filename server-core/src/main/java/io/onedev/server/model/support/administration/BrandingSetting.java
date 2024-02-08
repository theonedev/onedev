package io.onedev.server.model.support.administration;

import java.io.Serializable;

public class BrandingSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String DEFAULT_NAME = "OneDev";
	
	private String name = DEFAULT_NAME;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
