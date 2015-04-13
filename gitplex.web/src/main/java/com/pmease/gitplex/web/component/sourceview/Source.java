package com.pmease.gitplex.web.component.sourceview;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Source implements Serializable {

	private final String revision;
	
	private final String path;
	
	private final String content;
	
	private final int activeLine;
	
	public Source(String revision, String path, String content, int activeLine) {
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

	public int getActiveLine() {
		return activeLine;
	}
	
}
