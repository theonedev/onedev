package org.server.plugin.report.checkstyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.code.CodeProblem.Severity;

public class ViolationFile implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String path;
	
	private final List<Violation> violations = new ArrayList<>();
	
	private transient Integer numOfErrors;
	
	private transient Integer numOfWarnings;
	
	private transient Integer numOfInfos;
	
	public ViolationFile(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public List<Violation> getViolations() {
		return violations;
	}
	
	public int getNumOfErrors() {
		if (numOfErrors == null)
			numOfErrors = (int) violations.stream().filter(it->it.getSeverity() == Severity.ERROR).count();
		return numOfErrors;
	}

	public int getNumOfWarnings() {
		if (numOfWarnings == null)
			numOfWarnings = (int) violations.stream().filter(it->it.getSeverity() == Severity.WARNING).count();
		return numOfWarnings;
	}
	
	public int getNumOfInfos() {
		if (numOfInfos == null)
			numOfInfos = (int) violations.stream().filter(it->it.getSeverity() == Severity.INFO).count();
		return numOfInfos;
	}
	
	public static class Violation extends AbstractViolation {

		private static final long serialVersionUID = 1L;
		
		private final Severity severity;
		
		private final String rule;
		
		public Violation(Severity severity, String message, String line, @Nullable String column, String rule) {
			super(message, line, column);
			this.severity = severity;
			this.rule = rule;
		}

		public Severity getSeverity() {
			return severity;
		}

		public String getRule() {
			return rule;
		}
		
	}
}