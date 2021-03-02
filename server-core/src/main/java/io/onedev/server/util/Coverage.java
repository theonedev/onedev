package io.onedev.server.util;

import java.io.Serializable;

public class Coverage implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int total;
	
	private final int covered;
	
	public Coverage(int total, int covered) {
		this.total = total;
		this.covered = covered;
	}

	public int getTotal() {
		return total;
	}

	public int getCovered() {
		return covered;
	}
	
	public int getPercent() {
		if (total != 0)
			return covered*100/total;
		else
			return 0;
	}
	
	@Override
	public String toString() {
		return covered + "/" + total;
	}
	
}
