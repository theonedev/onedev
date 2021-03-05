package io.onedev.server.code;

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

	public static enum Severity {ERROR, WARNING, INFO};
	
	private final PlanarRange range;
	
	private final String content;
	
	private final Severity severity;
	
	public CodeProblem(PlanarRange range, String content, Severity severity) {
		this.range = range;
		this.content = content;
		this.severity = severity;
	}

	public PlanarRange getRange() {
		return range;
	}

	public String getContent() {
		return content;
	}

	public Severity getSeverity() {
		return severity;
	}
	
	public CodeProblem normalizeRange(List<String> lines) {
		return new CodeProblem(range.normalize(lines), content, severity);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CodeProblem))
			return false;
		if (this == other)
			return true;
		CodeProblem otherProblem = (CodeProblem) other;
		return new EqualsBuilder()
				.append(range, otherProblem.range)
				.append(content, otherProblem.content)
				.append(severity, otherProblem.severity)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(range)
				.append(content)
				.append(severity)
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
