package com.pmease.commons.git;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Blame {
	
	private final BriefCommit commit;

	private final List<Range> ranges;
	
	public Blame(BriefCommit commit, List<Range> ranges) {
		this.commit = commit;
		this.ranges = ranges;
	}

	public BriefCommit getCommit() {
		return commit;
	}
	
	public List<Range> getRanges() {
		return ranges;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(commit.getHash()).append(": ");
		for (Range range: ranges) 
			builder.append(range).append(", ");
		
		return StringUtils.stripEnd(builder.toString(), ", ");
	}

	public static class Range {
		
		private final int beginLine; // 0-indexed, inclusive
		
		private final int endLine; // 0-indexed, exclusive
		
		public Range(int beginLine, int endLine) {
			this.beginLine = beginLine;
			this.endLine = endLine;
		}

		public int getBeginLine() {
			return beginLine;
		}

		public int getEndLine() {
			return endLine;
		}

		@Override
		public String toString() {
			return beginLine + "-" + endLine;
		}
		
	}
}
