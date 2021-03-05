package org.server.plugin.report.checkstyle;

import javax.annotation.Nullable;

import io.onedev.server.code.CodeProblem.Severity;

public class CheckstyleViolation extends AbstractViolation {

	private static final long serialVersionUID = 1L;

	private final Severity severity;
	
	private final String file;
	
	private final String rule;
	
	public CheckstyleViolation(Severity severity, String message, String line, @Nullable String column, 
			String file, String rule) {
		super(message, line, column);
		this.severity = severity;
		this.file = file;
		this.rule = rule;
	}

	public Severity getSeverity() {
		return severity;
	}

	public String getFile() {
		return file;
	}

	public String getRule() {
		return rule;
	}
	
}
