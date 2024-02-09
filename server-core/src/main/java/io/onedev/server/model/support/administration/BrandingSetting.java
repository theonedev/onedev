package io.onedev.server.model.support.administration;

import java.io.Serializable;

public class BrandingSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String DEFAULT_NAME = "OneDev";
	
	public static final String DEFAULT_URL = "https://onedev.io";
	
	private String name = DEFAULT_NAME;
	
	private String url = DEFAULT_URL;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public static boolean isOEM(String brandName) {
		return "GitOn".equals(brandName);
	}
	
	public boolean isOEM() {
		return isOEM(getName());
	}
	
}
