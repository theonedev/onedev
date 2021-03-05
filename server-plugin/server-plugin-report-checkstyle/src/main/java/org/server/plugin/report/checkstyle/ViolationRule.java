package org.server.plugin.report.checkstyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.code.CodeProblem.Severity;

public class ViolationRule implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final Severity severity;
	
	private final List<Violation> violations = new ArrayList<>();
	
	public ViolationRule(String name, Severity severity) {
		this.name = name;
		this.severity = severity;
	}
	
	public String getName() {
		return name;
	}

	public Severity getSeverity() {
		return severity;
	}

	public List<Violation> getViolations() {
		return violations;
	}

	public static class Violation extends AbstractViolation {

		private static final long serialVersionUID = 1L;

		private final String file;
		
		public Violation(String message, String line, @Nullable String column, String file) {
			super(message, line, column);
			this.file = file;
		}

		public String getFile() {
			return file;
		}
		
	}
}
