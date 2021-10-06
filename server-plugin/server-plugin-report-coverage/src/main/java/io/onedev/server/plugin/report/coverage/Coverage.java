package io.onedev.server.plugin.report.coverage;

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
		if (total != 0)
			return covered + "/" + total;
		else
			return "(no data)";
	}
	
	public Coverage mergeWith(Coverage coverage) {
		return new Coverage(total + coverage.total, covered + coverage.covered);
	}
	
}
