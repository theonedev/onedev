package io.onedev.server.git.command;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
public class FileChange implements Serializable {
	
	private final String oldPath;
	
	private final String newPath;
	
	private final int additions;
	
	private final int deletions;
	
	public FileChange(@Nullable String oldPath, @Nullable String newPath, int additions, int deletions) {
		this.oldPath = oldPath;
		this.newPath = newPath;
		this.additions = additions;
		this.deletions = deletions;
	}

	@Nullable
	public String getOldPath() {
		return oldPath;
	}
	
	@Nullable
	public String getNewPath() {
		return newPath;
	}

	public int getAdditions() {
		return additions;
	}

	public int getDeletions() {
		return deletions;
	}

	public Collection<String> getPaths() {
		Set<String> paths = new HashSet<>();
		if (getOldPath() != null)
			paths.add(getOldPath());
		if (getNewPath() != null)
			paths.add(getNewPath());
		return paths;
	}
	
}