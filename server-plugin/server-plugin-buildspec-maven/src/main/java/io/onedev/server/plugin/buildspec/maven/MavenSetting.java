package io.onedev.server.plugin.buildspec.maven;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class MavenSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private String mavenPath;

	@Editable
	@NotEmpty
	public String getMavenPath() {
		return mavenPath;
	}

	public void setMavenPath(String mavenPath) {
		this.mavenPath = mavenPath;
	}
	
}
