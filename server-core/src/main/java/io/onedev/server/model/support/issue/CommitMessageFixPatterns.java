package io.onedev.server.model.support.issue;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.service.ProjectService;
import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.model.Project;
import io.onedev.server.validation.Validatable;
import io.onedev.server.validation.validator.ProjectKeyValidator;
import io.onedev.server.validation.validator.ProjectPathValidator;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.onedev.server.entityreference.ReferenceUtils.mayContainReferences;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.unbescape.java.JavaEscape.unescapeJava;

@Editable
@ClassValidating
public class CommitMessageFixPatterns implements Serializable, Validatable {
	private static final long serialVersionUID = 1L;
	
	private List<Entry> entries = new ArrayList<>();
	
	private transient List<Pattern> patterns;

	@Editable
	@OmitName
	public List<Entry> getEntries() {
		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	private List<Pattern> getPatterns() {
		if (patterns == null) {
			patterns = new ArrayList<>();
			for (var entry: entries) {
				var builder = new StringBuilder();
				if (entry.getPrefix() != null)
					builder.append("(").append(entry.getPrefix()).append(")");
				builder.append(String.format("(issue\\s+)?(((?<projectPath>%s)?#)|(?<projectKey>%s)-)(?<number>\\d+)", unescapeJava(ProjectPathValidator.PATTERN.pattern()), unescapeJava(ProjectKeyValidator.PATTERN.pattern())));
				if (entry.getSuffix() != null)
					builder.append("(").append(entry.getSuffix()).append(")");
				patterns.add(Pattern.compile(builder.toString(), CASE_INSENSITIVE));
			}
		}
		return patterns;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		int index = 0;
		for (var entry: entries) {
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
				for (var pattern: getPatterns()) {
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
	public static class Entry implements Serializable {

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
