package com.pmease.gitplex.search.hit;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.google.common.base.Preconditions;

public class ContentHit extends QueryHit {

	private final int lineNo;
	
	private final String line;
	
	private final List<Match> matches;
	
	public ContentHit(String blobPath, String line, int lineNo, List<Match> matches) {
		super(blobPath);
		
		Preconditions.checkArgument(!matches.isEmpty(), "Matches should not be empty");
		this.line = line;
		this.lineNo = lineNo;
		this.matches = matches;
	}

	public int getLineNo() {
		return lineNo;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getBlobPath()).append(":").append(getLineNo()).append(":");
		builder.append(line.substring(0, matches.get(0).getStart()));
		for (int i=0; i<matches.size(); i++) {
			Match match = matches.get(i);
			builder.append("[").append(line.substring(match.start, match.end)).append("]");
			if (i+1<matches.size())
				builder.append(line.substring(match.end, matches.get(i+1).start));
			else
				builder.append(line.substring(match.end));
		}
		return builder.toString();
	}

	public static class Match {
		
		private final int start;
		
		private final int end;
		
		public Match(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}
		
	}

	@Override
	public Component render(String componentId) {
		return new Label(componentId, toString());
	}
}
