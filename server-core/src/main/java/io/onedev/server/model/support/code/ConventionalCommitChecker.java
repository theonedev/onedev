package io.onedev.server.model.support.code;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.validation.constraints.NotEmpty;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.RegExp;

@Editable(order=100, name="Conventional Commit")
public class ConventionalCommitChecker implements CommitMessageChecker {

	private static final long serialVersionUID = 1L;
	
	private static final Pattern CONVENTIONAL_COMMIT_SUBJECT = Pattern.compile(
			"^((\\p{L}+)(\\(([\\p{L}\\p{N}]+([\\-/ ][\\p{L}\\p{N}]+)*)\\))?!?: [^ ].+)|^(revert \".*\")", 
			UNICODE_CHARACTER_CLASS | CASE_INSENSITIVE);

	private List<String> commitTypes = new ArrayList<>();

	private List<String> commitScopes = new ArrayList<>();

	private boolean checkCommitMessageFooter;
	
	private String commitMessageFooterPattern;
	
	private List<String> commitTypesForFooterCheck = new ArrayList<>();

	@Editable(order=100, placeholder = "Arbitrary type", description = "Optionally specify valid " +
			"types of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary type")
	public List<String> getCommitTypes() {
		return commitTypes;
	}

	public void setCommitTypes(List<String> commitTypes) {
		this.commitTypes = commitTypes;
	}

	@Editable(order=200, placeholder = "Arbitrary scope", description = "Optionally specify valid " +
			"scopes of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary scope")
	public List<String> getCommitScopes() {
		return commitScopes;
	}

	public void setCommitScopes(List<String> commitScopes) {
		this.commitScopes = commitScopes;
	}

	@Editable(order=300)
	public boolean isCheckCommitMessageFooter() {
		return checkCommitMessageFooter;
	}

	public void setCheckCommitMessageFooter(boolean checkCommitMessageFooter) {
		this.checkCommitMessageFooter = checkCommitMessageFooter;
	}

	@Editable(order=400, description = "A " +
			"<a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java regular expression</a> " +
			"to validate commit message footer")
	@DependsOn(property="checkCommitMessageFooter")
	@RegExp
	@NotEmpty
	public String getCommitMessageFooterPattern() {
		return commitMessageFooterPattern;
	}

	public void setCommitMessageFooterPattern(String commitMessageFooterPattern) {
		this.commitMessageFooterPattern = commitMessageFooterPattern;
	}

	@Editable(order=500, description = "Optionally specify commit types applicable for commit message footer check (hit ENTER to add value). " +
			"Leave empty to all types")
	@DependsOn(property="checkCommitMessageFooter")
	public List<String> getCommitTypesForFooterCheck() {
		return commitTypesForFooterCheck;
	}

	public void setCommitTypesForFooterCheck(List<String> commitTypesForFooterCheck) {
		this.commitTypesForFooterCheck = commitTypesForFooterCheck;
	}
	
	@Override
	public String checkCommitMessage(String commitMessage, boolean merged) {
		var lines = Splitter.on('\n').trimResults().splitToList(commitMessage);
		if (lines.isEmpty())
			return "Message is empty";
		
		if (!merged) {
			var matcher = CONVENTIONAL_COMMIT_SUBJECT.matcher(lines.get(0));
			if (matcher.matches()) {
				if (matcher.group(1) != null) {
					var type = matcher.group(2);
					if (!commitTypes.isEmpty() && !commitTypes.contains(type))
						return "Line 1: Unexpected type '" + type + "': Should be one of [" + Joiner.on(',').join(commitTypes) + "]";
					var scope = matcher.group(4);
					if (scope != null && !commitScopes.isEmpty() && !commitScopes.contains(scope))
						return "Line 1: Unexpected scope '" + scope + "': Should be one of [" + Joiner.on(',').join(commitScopes) + "]";
					if (checkCommitMessageFooter && (commitTypesForFooterCheck.isEmpty() || commitTypesForFooterCheck.contains(type))) {
						var size = lines.size();
						if (size < 3 || lines.get(size-1).length() == 0 
								|| lines.get(size-2).length() != 0 || lines.get(size-3).length() == 0) {
							return "A footer is expected as last line and exactly one blank line should precede the footer";
						} else if (!lines.get(size-1).matches(commitMessageFooterPattern)) {
							return "Unexpected footer format: Should match pattern '" + commitMessageFooterPattern + "'";
						}
					}
				}
			} else {
				return "Line 1: Subject is expected of either a git revert message, or format: <type>[optional (scope)][!]: <description>";
			}
		}
	
		for (int i=1; i<lines.size(); i++) {
			var line = lines.get(i);
			if (line.length() != 0) {
				if (i != 2) 
					return "One and only one blank line is expected between subject and body/footer";
				break;
			}
		}
		
		return null;
	}
	
}
