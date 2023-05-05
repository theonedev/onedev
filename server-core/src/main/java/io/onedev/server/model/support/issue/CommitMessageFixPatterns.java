package io.onedev.server.model.support.issue;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.entityreference.ReferenceParser;
import io.onedev.server.util.Pair;
import io.onedev.server.validation.Validatable;
import io.onedev.server.validation.validator.ProjectPathValidator;
import org.unbescape.java.JavaEscape;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

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
				builder.append("(issue\\s+)?");
				builder.append("(?<project>").append(JavaEscape.unescapeJava(ProjectPathValidator.PATTERN.pattern())).append(")?");
				builder.append("#(?<issue>\\d+)");
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

	public List<Pair<String, Long>> parseFixedIssues(String commitMessage) {
		List<Pair<String, Long>> issues = new ArrayList<>();
		
		commitMessage = commitMessage.toLowerCase();
		for (var line: StringUtils.splitAndTrim(commitMessage, "\n")) {
			if (ReferenceParser.fastScan(commitMessage)) {
				for (var pattern: getPatterns()) {
					Matcher matcher = pattern.matcher(line);
					while (matcher.find()) {
						String projectPath = matcher.group("project");
						Long issueNumber = Long.parseLong(matcher.group("issue"));
						issues.add(new Pair<>(projectPath, issueNumber));
					}
				}
			}
		}
		return issues;
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
