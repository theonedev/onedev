package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.util.validation.annotation.DnsName;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Password;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class Secret implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String description;
	
	private String value;
	
	private String authorizedBranches;
	
	@Editable(order=100)
	@NotEmpty
	@DnsName
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=150)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	@Editable(order=300, name="Authorize Branches", description=""
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
		return SuggestionUtils.suggestBranches(Project.get(), matchWith);
	}
	
	public boolean isAuthorized(Project project, ObjectId commitId) {
		return authorizedBranches == null || project.isCommitOnBranches(commitId, authorizedBranches);
	}
	
}
