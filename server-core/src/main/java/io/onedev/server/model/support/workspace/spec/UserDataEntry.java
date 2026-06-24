package io.onedev.server.model.support.workspace.spec;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import org.jspecify.annotations.Nullable;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.UserDataEntryFacade;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Path;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class UserDataEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private String path;

	private String excludes;

	@Editable(order=100, description = "Specify data path to persist across workspaces. Only absolute path is accepted")
	@Path(Path.Type.ABSOLUTE)
	@Interpolative(variableSuggester="suggestVariables", exampleVar = "/")
	@NotEmpty
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Editable(order=200, placeholder = "None", description = """
			Optionally specify directories or files relative to data path to exclude. 
			Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. 
			Multiple excludes should be separated by space, and single exclude containing space should be quoted""")
	@Interpolative(variableSuggester="suggestVariables")
	public String getExcludes() {
		return excludes;
	}

	public void setExcludes(String excludes) {
		this.excludes = excludes;
	}

	public UserDataEntryFacade getFacade() {
		return new UserDataEntryFacade(path, excludes);
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

	public static UserDataEntry of(String path, @Nullable String excludes) {
		var entry = new UserDataEntry();
		entry.setPath(path);
		entry.setExcludes(excludes);
		return entry;
	}

}
