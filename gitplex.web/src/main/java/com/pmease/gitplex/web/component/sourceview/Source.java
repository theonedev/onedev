package com.pmease.gitplex.web.component.sourceview;

public class Source {

	private final String revision;
	
	private final String path;
	
	private final String content;
	
	public Source(String revision, String path, String content) {
		this.revision = revision;
		this.path = path;
		this.content = content;
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
	
}
