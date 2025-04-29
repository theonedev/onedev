package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.validation.Validatable;

@Editable
@ClassValidating
public class ExternalIssueTransformers implements Serializable, Validatable {
	private static final long serialVersionUID = 1L;
	
	private List<Entry> entries = new ArrayList<>();
	
	@Editable
	@OmitName
	public List<Entry> getEntries() {
		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		int index = 0;
		for (var entry: entries) {
			if (entry.getPattern() != null) {
				try {
					Pattern.compile(entry.getPattern());
				} catch (Exception e) {
					isValid = false;
					var message = e.getMessage();
					if (message == null)
						message = "Malformed regular expression";
					context.buildConstraintViolationWithTemplate(message)
							.addPropertyNode("entries")
							.addPropertyNode("pattern")
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

	@Editable
	public static class Entry implements Serializable {

		private static final long serialVersionUID = 1L;

		private String pattern;

		private String replaceWith;

		@Editable(order = 100, name="Issue Pattern", description = "Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> " +
				"to match issue references. For instance: <code>(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)</code>")
		@NotEmpty
		public String getPattern() {
			return pattern;
		}

		public void setPattern(String pattern) {
			this.pattern = pattern;
		}

		@Editable(order = 200, name="Replace With", description = "Specify text to replace matched issue references with, for instance: <code>$1&lt;a href='https://tracker.example.com/issues/$2'&gt;$2&lt;/a&gt;</code>. Here <code>$1</code> and <code>$2</code> represent catpure groups in the issue pattern")
		@NotEmpty
		public String getReplaceWith() {
			return replaceWith;
		}

		public void setReplaceWith(String replaceWith) {
			this.replaceWith = replaceWith;
		}

	}	
}
