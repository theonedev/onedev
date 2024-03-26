package io.onedev.server.codequality;

import io.onedev.commons.utils.PlanarRange;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

import static java.util.Comparator.comparingInt;

public class CodeProblem implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Severity {CRITICAL, HIGH, MEDIUM, LOW};

	private final Severity severity;
	
	private final String blobPath;

	private final PlanarRange range;
	
	private final String message;
	
	public CodeProblem(Severity severity, String blobPath, @Nullable PlanarRange range, String message) {
		this.severity = severity;
		this.blobPath = blobPath;
		this.range = range;
		this.message = message;
	}

	public Severity getSeverity() {
		return severity;
	}

	public String getBlobPath() {
		return blobPath;
	}

	@Nullable
	public PlanarRange getRange() {
		return range;
	}

	public String getMessage() {
		return message;
	}

	public CodeProblem normalizeRange(List<String> lines) {
		return new CodeProblem(severity, blobPath, range!=null?range.normalize(lines):null, message);
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
				.append(blobPath, otherProblem.blobPath)
				.append(range, otherProblem.range)
				.append(message, otherProblem.message)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(severity)
				.append(blobPath)
				.append(range)
				.append(message)
				.toHashCode();
	}
	
	public static Map<Integer, List<CodeProblem>> groupByLine(Collection<CodeProblem> problems) {
		Map<Integer, List<CodeProblem>> problemsByLine = new HashMap<>();
		
		for (CodeProblem problem: problems) {
			PlanarRange range = problem.getRange();
			if (range != null) {
				int line = range.getFromRow();
				List<CodeProblem> problemsAtLine = problemsByLine.get(line);
				if (problemsAtLine == null) {
					problemsAtLine = new ArrayList<>();
					problemsByLine.put(line, problemsAtLine);
				}
				problemsAtLine.add(problem);
			}
		}
		
		for (List<CodeProblem> value: problemsByLine.values()) {
			value.sort(comparingInt(o -> o.getSeverity().ordinal()));
		}
		
		return problemsByLine;
	}
	
}
