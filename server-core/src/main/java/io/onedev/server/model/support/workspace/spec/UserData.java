package io.onedev.server.model.support.workspace.spec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Path;
import io.onedev.server.annotation.PathSegment;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class UserData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String key;
		
	private List<String> paths = new ArrayList<>();
		
	private String changeDetectionExcludes;
	
	@Editable(order=100, name="Data Key", description = """
			Specify a key to identify the user data. Data with same key will be shared across workspaces 
			even if project is different""")
	@PathSegment
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Editable(order=200, name="Data Paths", description = "Specify data paths to persist across workspaces. Only absolute path is accepted")
	@Valid
	@Path(Path.Type.ABSOLUTE)
	@Interpolative(variableSuggester="suggestVariables", exampleVar = "/")
	@Size(min=1, max=100)
	public List<String> getPaths() {
		return paths;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}

	@Editable(order=300, description = """
			Optionally specify files relative to data path to ignore when detect data changes. 
			Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. 
			Multiple files should be separated by space, and single file containing space should be quoted""")
	@Interpolative(variableSuggester="suggestVariables")
	public String getChangeDetectionExcludes() {
		return changeDetectionExcludes;
	}
	
	public void setChangeDetectionExcludes(String changeDetectionExcludes) {
		this.changeDetectionExcludes = changeDetectionExcludes;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}
		
}
