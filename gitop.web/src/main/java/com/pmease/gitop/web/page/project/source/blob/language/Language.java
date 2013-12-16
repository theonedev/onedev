package com.pmease.gitop.web.page.project.source.blob.language;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class Language implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty
	String name;
	
	@JsonProperty
	private String[] mimeTypes;
	
	@JsonProperty
	private String mode;
	
	@JsonProperty
	private String aceMode;
	
	Language() {
	}
	
	public Language(final String name, final String mode, final String aceMode, final String[] mimeTypes) {
		this.name = name;
		this.mode = mode;
		this.aceMode = aceMode;
		this.mimeTypes = mimeTypes;
	}

	public String getName() {
		return name;
	}

	public String[] getMimeTypes() {
		return mimeTypes;
	}

	public String getMode() {
		return mode == null ? "no-highlight" : mode;
	}
	
	public String getAceMode() {
		return Strings.isNullOrEmpty(aceMode) ? "text" : aceMode;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("name", name)
				.add("mode", mode)
				.add("mimeTypes", mimeTypes)
				.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof Language)) return false;
		
		Language rhs = (Language) other;
		return Objects.equal(name, rhs.name);
	}
}
