package com.pmease.gitop.web.page.repository.source.commit.diff.patch;

public class DiffStat {
	int additions;
	int deletions;

	public int getAdditions() {
		return additions;
	}

	public int getDeletions() {
		return deletions;
	}

	public int getTotalChanges() {
		return additions + deletions;
	}
	
	@Override
	public String toString() {
		return additions + " additions & " + deletions + " deletions";
	}
}
