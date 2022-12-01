package io.onedev.server.util;

import java.io.Serializable;

public class LongRange implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final long start, end;
	
	public LongRange(long start, long end) {
		this.start = start;
		this.end = end;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

}
