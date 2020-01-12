package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.LinearRange;
import io.onedev.server.model.Project;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.validation.annotation.SecretName;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Password;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.SuggestionProvider;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class JobSecret implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String value;
	
	private String authorizedBranches;
	
	@Editable(order=100)
	@SuggestionProvider("suggestNames")
	@NotEmpty
	@SecretName
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@SuppressWarnings("unused")
	private static List<InputCompletion> suggestNames(InputStatus inputStatus) {
		Project project = Project.get();
		List<InputCompletion> suggestions = new ArrayList<>();
		if (project != null) {
			for (JobSecret secret: project.getBuildSetting().getInheritedSecrets(project)) {
				LinearRange match = LinearRange.match(secret.getName(), inputStatus.getContentBeforeCaret());
				if (match != null) {
					InputCompletion suggestion = new InputCompletion(secret.getName(), 
							secret.getName() + inputStatus.getContentAfterCaret(), 
							secret.getName().length(), "override inherited", match);
					suggestions.add(suggestion);
				}
			}
		} 
		return suggestions;
	}

	@Editable(order=200)
	@Password
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Editable(order=300, description=""
			+ "Optionally specify space-separated branches to authorize.\n"
			+ "Only builds from authorized branches can access this secret.\n"
			+ "Use * or ? for wildcard match. Leave empty to authorize all branches")
	@Patterns(suggester = "suggestBranches")
	@NameOfEmptyValue("All")
	public String getAuthorizedBranches() {
		return authorizedBranches;
	}

	public void setAuthorizedBranches(String authorizedBranches) {
		this.authorizedBranches = authorizedBranches;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggestBranches(project, matchWith);
		else
			return new ArrayList<>();
	}
	
	public boolean isAuthorized(Project project, ObjectId commitId) {
		return authorizedBranches == null || project.isCommitOnBranches(commitId, authorizedBranches);
	}
	
	public boolean isAuthorized(Project project, String branch) {
		return authorizedBranches == null 
				|| PatternSet.parse(authorizedBranches).matches(new PathMatcher(), branch);
	}
	
}
