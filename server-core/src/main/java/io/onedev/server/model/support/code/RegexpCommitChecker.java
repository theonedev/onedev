package io.onedev.server.model.support.code;

import java.util.regex.PatternSyntaxException;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.RegExp;

@Editable(order=200, name="Regular Expression")
public class RegexpCommitChecker implements CommitMessageChecker {

	private static final long serialVersionUID = 1L;
	
	private String pattern;

	private String explanation;

	@Editable(order=100, description = "Specify a " +
			"<a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java regular expression</a> pattern " +
			"to validate the entire commit message. Use <code>(?s)</code> flag at the beginning for multi-line matching")
	@RegExp
	@NotEmpty
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Editable(order=200, placeholder = "No explanation", description = """
		Optionally specify an explanation for the regular expression. This will be displayed to the user if validation fails""")
	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}
	@Override
	public String checkCommitMessage(String commitMessage, boolean merged) {
		if (merged) {
			// Skip validation for merge commits
			return null;
		}
		
		try {
			if (!commitMessage.matches(pattern)) {
				return explanation != null ? explanation : "Commit message does not match required regular expression: " + pattern;
			}
		} catch (PatternSyntaxException e) {
			return "Invalid regular expression: " + e.getMessage();
		}
		
		return null;
	}
	
}
