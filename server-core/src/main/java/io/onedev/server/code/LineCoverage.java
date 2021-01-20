package io.onedev.server.code;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LineCoverage implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int fromLine;
	
	private final int toLine;
	
	private final int testCount;
	
	public LineCoverage(int fromLine, int toLine, int testCount) {
		this.fromLine = fromLine;
		this.toLine = toLine;
		this.testCount = testCount;
	}

	public int getFromLine() {
		return fromLine;
	}

	public int getToLine() {
		return toLine;
	}

	public int getTestCount() {
		return testCount;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LineCoverage))
			return false;
		if (this == other)
			return true;
		LineCoverage otherCoverage = (LineCoverage) other;
		return new EqualsBuilder()
				.append(fromLine, otherCoverage.fromLine)
				.append(toLine, otherCoverage.toLine)
				.append(testCount, otherCoverage.testCount)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(fromLine)
				.append(toLine)
				.append(testCount)
				.toHashCode();
	}
	
	public static Map<Integer, Integer> groupByLine(Collection<LineCoverage> coverages) {
		Map<Integer, Integer> coverageByLine = new HashMap<>();
		for (LineCoverage coverage: coverages) {
			for (int line = coverage.getFromLine(); line <= coverage.getToLine(); line++) {
				Integer testCount = coverageByLine.get(line);
				if (testCount != null) 
					coverageByLine.put(line, testCount + coverage.getTestCount());
				else
					coverageByLine.put(line, coverage.getTestCount());
			}
		}
		return coverageByLine;
	}
	
}
