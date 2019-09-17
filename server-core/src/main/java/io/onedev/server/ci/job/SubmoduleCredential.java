package io.onedev.server.ci.job;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class SubmoduleCredential extends Authentication {

	private static final long serialVersionUID = 1L;

	private String url;
	
	@Editable(order=100)
	@NotEmpty
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}