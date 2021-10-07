package io.onedev.server.codequality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.onedev.commons.utils.PlanarRange;

public class CodeProblem implements Serializable {

	private static final long serialVersionUID = 1L;

	public static enum Severity {HIGH, MEDIUM, LOW};

	private final Severity severity;
	
	private final String type;
	
	private final String blobPath;

	private final PlanarRange range;
	
	private final String message;
	
	public CodeProblem(Severity severity, String type, String blobPath, PlanarRange range, String message) {
		this.severity = severity;
		this.type = type;
		this.blobPath = blobPath;
		this.range = range;
		this.message = message;
	}

	public Severity getSeverity() {
		return severity;
	}
	
	public String getType() {
		return type;
	}

	public String getBlobPath() {
		return blobPath;
	}

	public PlanarRange getRange() {
		return range;
	}

	public String getMessage() {
		return message;
	}

	public CodeProblem normalizeRange(List<String> lines) {
		return new CodeProblem(severity, type, blobPath, range.normalize(lines), message);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CodeProblem))
			return false;
		if (this == other)
			return true;
		CodeProblem otherProblem = (CodeProblem) other;
		return new EqualsBuilder()
				.append(severity, otherProblem.severity)
				.append(type, otherProblem.type)
				.append(blobPath, otherProblem.blobPath)
				.append(range, otherProblem.range)
				.append(message, otherProblem.message)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(severity)
				.append(type)
				.append(blobPath)
				.append(range)
				.append(message)
				.toHashCode();
	}
	
	public static Map<Integer, List<CodeProblem>> groupByLine(Collection<CodeProblem> problems) {
		Map<Integer, List<CodeProblem>> problemsByLine = new HashMap<>();
		
		for (CodeProblem problem: problems) {
			PlanarRange position = problem.getRange();
			int line = position.getFromRow();
			List<CodeProblem> problemsAtLine = problemsByLine.get(line);
			if (problemsAtLine == null) {
				problemsAtLine = new ArrayList<>();
				problemsByLine.put(line, problemsAtLine);
			}
			problemsAtLine.add(problem);
		}
		
		for (List<CodeProblem> value: problemsByLine.values()) {
			value.sort((o1, o2)->(int)(o1.getSeverity().ordinal()-o2.getSeverity().ordinal()));
		}
		
		return problemsByLine;
	}
	
}
