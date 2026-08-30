package io.onedev.server.git;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

public class Submodule implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String SEPARATOR = ":";

	private final @Nullable String url;
	
	private final String commitHash;
	
	public Submodule(@Nullable String url, String commitHash) {
		this.url = url;
		this.commitHash = commitHash;
	}

	public @Nullable String getUrl() {
		return url;
	}

	public String getCommitId() {
		return commitHash;
	}
	
	@Override
	public String toString() {
		if (url != null)
			return url + SEPARATOR + commitHash;
		else
			return commitHash;
	}
	
	public static Submodule fromString(String str) {
		int separatorIndex = str.lastIndexOf(SEPARATOR);
		if (separatorIndex != -1) {
			String url = str.substring(0, separatorIndex);
			String commitHash = str.substring(separatorIndex + SEPARATOR.length());
			return new Submodule(url, commitHash);
		} else {
			return new Submodule(null, str);
		}
	}
}
