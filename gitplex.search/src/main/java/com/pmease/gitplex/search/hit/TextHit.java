package com.pmease.gitplex.search.hit;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.base.Preconditions;

public class TextHit extends QueryHit {

	private static final long serialVersionUID = 1L;
	
	private final int lineNo;
	
	private final String line;
	
	private final List<Range> ranges;
	
	public TextHit(String blobPath, String line, int lineNo, List<Range> ranges) {
		super(blobPath);
		
		Preconditions.checkArgument(!ranges.isEmpty(), "Ranges should not be empty");
		this.line = line;
		this.lineNo = lineNo;
		this.ranges = ranges;
	}

	@Override
	public int getLineNo() {
		return lineNo;
	}
	
	public String getLine() {
		return line;
	}
	
	public List<Range> getRanges() {
		return ranges;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getLineNo()).append(": ");
		builder.append(line.substring(0, ranges.get(0).getStart()));
		for (int i=0; i<ranges.size(); i++) {
			Range range = ranges.get(i);
			builder.append("[").append(line.substring(range.start, range.end)).append("]");
			if (i+1<ranges.size())
				builder.append(line.substring(range.end, ranges.get(i+1).start));
			else
				builder.append(line.substring(range.end));
		}
		return builder.toString();
	}

	public static class Range implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private final int start;
		
		private final int end;
		
		public Range(int start, int end) {
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
		return new TextHitPanel(componentId, this);
	}

	@Override
	public ResourceReference getIcon() {
		return new PackageResourceReference(FileHit.class, "bullet.png");
	}

	@Override
	public String getScope() {
		throw new UnsupportedOperationException();
	}

}
