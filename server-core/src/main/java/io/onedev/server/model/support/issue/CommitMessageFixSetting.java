package io.onedev.server.model.support.issue;

import static io.onedev.server.entityreference.ReferenceUtils.mayContainReferences;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.unbescape.java.JavaEscape.unescapeJava;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.model.Project;
import io.onedev.server.service.ProjectService;
import io.onedev.server.validation.Validatable;
import io.onedev.server.validation.validator.ProjectKeyValidator;
import io.onedev.server.validation.validator.ProjectPathValidator;

@Editable
@ClassValidating
public class CommitMessageFixSetting implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_FIX_SUGGESTION = """
		When a commit is intended to resolve/fix/close an issue, add the issue reference in the commit message \
		footer as a separate line, for instance: 
		
		Fixes #100
		Fixes PROJ-100
		
		Note that this footer should not be added to fixup commits.""";

	private String fixSuggestion = DEFAULT_FIX_SUGGESTION;
		
	private List<FixPattern> fixPatterns = new ArrayList<>();

	private transient List<Pattern> parsedFixPatterns;

	public CommitMessageFixSetting() {
		var fixPattern = new FixPattern();
		fixPattern.setPrefix("(^|\\W)(fix|fixed|fixes|fixing|resolve|resolved|resolves|resolving|close|closed|closes|closing)[\\s:]+");
		fixPattern.setSuffix("(?=$|\\W)");
		fixPatterns.add(fixPattern);
		fixPattern = new FixPattern();
		fixPattern.setPrefix("\\(\\s*");
		fixPattern.setSuffix("\\s*\\)\\s*$");
		fixPatterns.add(fixPattern);
	}
	
	@Editable(order=100, name="Fix Suggestion", description="Specify how coding agents "
			+ "should reference an issue in commit messages when the commit is intended to fix an issue")
	@Multiline
	@NotEmpty
	public String getFixSuggestion() {
		return fixSuggestion;
	}

	public void setFixSuggestion(String fixSuggestion) {
		this.fixSuggestion = fixSuggestion;
	}

	@Editable(order=200, name="Fix Detection", description="Specify prefix/suffix patterns to detect fixed issues "
			+ "in commit messages. Each line of the commit message will be matched against each entry defined here")
	@NotNull
	@Valid
	public List<FixPattern> getFixPatterns() {
		return fixPatterns;
	}

	public void setFixPatterns(List<FixPattern> entries) {
		this.fixPatterns = entries;
	}

	private List<Pattern> getParsedFixPatterns() {
		if (parsedFixPatterns == null) {
			parsedFixPatterns = new ArrayList<>();
			for (var fixPattern: fixPatterns) {
				var builder = new StringBuilder();
				if (fixPattern.getPrefix() != null)
					builder.append("(").append(fixPattern.getPrefix()).append(")");
				builder.append(String.format("(issue\\s+)?(((?<projectPath>%s)?#)|(?<projectKey>%s)-)(?<number>\\d+)", unescapeJava(ProjectPathValidator.PATTERN.pattern()), unescapeJava(ProjectKeyValidator.PATTERN.pattern())));
				if (fixPattern.getSuffix() != null)
					builder.append("(").append(fixPattern.getSuffix()).append(")");
				parsedFixPatterns.add(Pattern.compile(builder.toString(), CASE_INSENSITIVE));
			}
		}
		return parsedFixPatterns;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		int index = 0;
		for (var entry: fixPatterns) {
			if (entry.getPrefix() != null) {
				try {
					Pattern.compile(entry.getPrefix());
				} catch (Exception e) {
					isValid = false;
					var message = e.getMessage();
					if (message == null)
						message = "Malformed regular expression";
					context.buildConstraintViolationWithTemplate(message)
							.addPropertyNode("entries")
							.addPropertyNode("prefix")
							.inIterable()
							.atIndex(index)
							.addConstraintViolation();
				}
			}
			if (entry.getSuffix() != null) {
				try {
					Pattern.compile(entry.getSuffix());
				} catch (Exception e) {
					isValid = false;
					var message = e.getMessage();
					if (message == null)
						message = "Malformed regular expression";
					context.buildConstraintViolationWithTemplate(message)
							.addPropertyNode("entries")
							.addPropertyNode("suffix")
							.inIterable()
							.atIndex(index)
							.addConstraintViolation();
				}
			}
			index++;
		}
		if (!isValid) 
			context.disableDefaultConstraintViolation();
		return isValid;
	}

	public List<IssueReference> parseFixedIssues(String commitMessage, Project currentProject) {
		Set<IssueReference> references = new LinkedHashSet<>();
		
		var projectService = OneDev.getInstance(ProjectService.class);
		for (var line: StringUtils.splitAndTrim(commitMessage, "\n")) {
			if (mayContainReferences(commitMessage)) {
				for (var pattern: getParsedFixPatterns()) {
					Matcher matcher = pattern.matcher(line);
					while (matcher.find()) {
						Project project;
						var projectKey = matcher.group("projectKey");
						if (projectKey != null) {
							project = projectService.findByKey(projectKey);
						} else {
							var projectPath = matcher.group("projectPath");
							if (projectPath != null)
								project = projectService.findByPath(projectPath);
							else 
								project = currentProject;
						}
						if (project != null) 
							references.add(new IssueReference(project, Long.parseLong(matcher.group("number"))));
					}
				}
			}
		}
		return new ArrayList<>(references);
	}	
	
	@Editable
	public static class FixPattern implements Serializable {

		private static final long serialVersionUID = 1L;

		private String prefix;

		private String suffix;

		@Editable(order = 100, name="Prefix Pattern", description = "Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> " +
				"before issue number")
		@NotEmpty
		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		@Editable(order = 200, name="Suffix Pattern", description = "Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> " +
				"after issue number")
		@NotEmpty
		public String getSuffix() {
			return suffix;
		}

		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}

	}	
}
