package io.onedev.server.git;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class Submodule implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String SEPARATOR = ":";

	private final String url;
	
	private final String commitHash;
	
	public Submodule(String url, String commitHash) {
		this.url = url;
		this.commitHash = commitHash;
	}

	public String getUrl() {
		return url;
	}

	public String getCommitId() {
		return commitHash;
	}
	
	@Override
	public String toString() {
		return url + SEPARATOR + commitHash;
	}
	
	public static Submodule fromString(String str) {
		String url = StringUtils.substringBeforeLast(str, SEPARATOR);
		String commitHash = StringUtils.substringAfterLast(str, SEPARATOR);
		return new Submodule(url, commitHash);
	}
}
