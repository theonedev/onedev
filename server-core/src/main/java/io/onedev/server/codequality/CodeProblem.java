package io.onedev.server.codequality;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class CodeProblem implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public enum Severity {
		CRITICAL, 
		HIGH, 
		MEDIUM, 
		LOW;
		
		public static Severity ofCvssScore(double cvssScore) {
			if (cvssScore >= 9.0)
				return CRITICAL;
			else if (cvssScore >= 7.0)
				return HIGH;
			else if (cvssScore >= 4.0)
				return MEDIUM;
			else
				return LOW;
		}
		
	};

	private final Severity severity;
	
	private final ProblemTarget target;
	
	private final String message;
	
	public CodeProblem(Severity severity, ProblemTarget target, String message) {
		this.severity = severity;
		this.target = target;
		this.message = message;
	}
	
	public Severity getSeverity() {
		return severity;
	}

	public ProblemTarget getTarget() {
		return target;
	}

	public String getMessage() {
		return message;
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
				.append(target, otherProblem.target)
				.append(message, otherProblem.message)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(severity)
				.append(target)
				.append(message)
				.toHashCode();
	}
	
}
