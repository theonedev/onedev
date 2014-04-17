package com.pmease.gitop.web.git.command;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.pmease.commons.git.BriefCommit;
import com.pmease.commons.git.GitContribInfo;

public class BlameEntry implements Serializable, Comparable<BlameEntry> {
	private static final long serialVersionUID = 1L;
	
	private final BriefCommit commit;
	private final int sourceLine;
	private final int resultLine;
	private final int numLines;
	
	public static class Builder {
		private String sha;
		private int sourceLine;
		private int resultLine;
		private int numLines;
		private GitContribInfo author;
		private GitContribInfo committer;
		private String summary;
		
		public Builder sha(String sha) {
			this.sha = sha;
			return this;
		}
		
		public Builder sourceLine(int sourceLine) {
			this.sourceLine = sourceLine;
			return this;
		}
		
		public Builder resultLine(int resultLine) {
			this.resultLine = resultLine;
			return this;
		}
		
		public Builder numLines(int numLines) {
			this.numLines = numLines;
			return this;
		}
		
		public Builder author(GitContribInfo author) {
			this.author = author;
			return this;
		}
		
		public Builder committer(GitContribInfo committer) {
			this.committer = committer;
			return this;
		}
		
		public Builder summary(String summary) {
			this.summary = summary;
			return this;
		}
		
		public BlameEntry build() {
			BriefCommit commit = new BriefCommit(sha, committer, author, summary);
			return new BlameEntry(commit, sourceLine, resultLine, numLines);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	BlameEntry(BriefCommit commit, int sourceLine, int resultLine, int numLines) {
		this.commit = commit;
		this.sourceLine = sourceLine;
		this.resultLine = resultLine;
		this.numLines = numLines;
	}

	public BriefCommit getCommit() {
		return commit;
	}

	public int getSourceLine() {
		return sourceLine;
	}

	public int getResultLine() {
		return resultLine;
	}

	public int getNumLines() {
		return numLines;
	}

	@Override
	public int compareTo(BlameEntry o) {
		return resultLine - o.resultLine;
	}
	
	@Override
	public String toString() {
		return commit.getHash() + " " + sourceLine + " " + resultLine + " " + numLines;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(commit, sourceLine, resultLine, numLines);
	}
}
