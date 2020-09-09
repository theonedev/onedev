package io.onedev.server.buildspec.job;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Build;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable
public abstract class JobReport implements Serializable {

	private static final long serialVersionUID = 1L;

	private String filePatterns;
	
	private transient PatternSet patternSet;

	@Editable(order=100, description="Specify files relative to OneDev workspace. Use * or ? for pattern match. "
			+ "<b>Note:</b> Type <tt>@</tt> to <a href='$docRoot/pages/variable-substitution.md' target='_blank' tabindex='-1'>insert variable</a>, use <tt>\\</tt> to escape normal occurrences of <tt>@</tt> or <tt>\\</tt>")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(interpolative = true, path=true)
	@NotEmpty
	public String getFilePatterns() {
		return filePatterns;
	}

	public void setFilePatterns(String filePatterns) {
		this.filePatterns = filePatterns;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return Job.suggestVariables(matchWith);
	}

	public PatternSet getPatternSet() {
		if (patternSet == null)
			patternSet = PatternSet.parse(getFilePatterns());
		return patternSet;
	}
	
	public abstract void process(Build build, File workspace, SimpleLogger logger);

}
