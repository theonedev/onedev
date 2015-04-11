package com.pmease.gitplex.web.component.sourceview;

import java.io.Serializable;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
public class Source implements Serializable {

	private final String revision;
	
	private final String path;
	
	private final String content;
	
	private final Integer activeLine;
	
	public Source(String revision, String path, String content, @Nullable Integer activeLine) {
		this.revision = revision;
		this.path = path;
		this.content = content;
		this.activeLine = activeLine;
	}

	public String getRevision() {
		return revision;
	}

	public String getPath() {
		return path;
	}

	public String getContent() {
		return content;
	}

	public Integer getActiveLine() {
		return activeLine;
	}
	
}
