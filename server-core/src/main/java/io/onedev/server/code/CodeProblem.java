package io.onedev.server.code;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.onedev.commons.utils.PlanarRange;

public class CodeProblem implements Serializable {

	private static final long serialVersionUID = 1L;

	public static enum Severity {ERROR, WARNING};
	
	private final PlanarRange position;
	
	private final String content;
	
	private final Severity severity;

	public CodeProblem(PlanarRange position, String content, Severity severity) {
		this.position = position;
		this.content = content;
		this.severity = severity;
	}

	public PlanarRange getPosition() {
		return position;
	}

	public String getContent() {
		return content;
	}

	public Severity getSeverity() {
		return severity;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CodeProblem))
			return false;
		if (this == other)
			return true;
		CodeProblem otherProblem = (CodeProblem) other;
		return new EqualsBuilder()
				.append(position, otherProblem.position)
				.append(content, otherProblem.content)
				.append(severity, otherProblem.severity)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(position)
				.append(content)
				.append(severity)
				.toHashCode();
	}
	
}
