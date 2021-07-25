package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.List;

import org.apache.shiro.authz.Permission;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.RoleChoice;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class SenderAuthorization implements Serializable {

	private static final long serialVersionUID = 1L;

	private String senderEmails;
	
	private String authorizedProjects;
	
	private String authorizedRoleName;
	
	@Editable(order=100, name="Applicable Senders", description="Specify space-separated sender "
			+ "email addresses applicable for this entry. Use '*' or '?' for wildcard match. "
			+ "Prefix with '-' to exclude. Leave empty to match all senders")
	@Patterns
	@NameOfEmptyValue("Any sender")
	public String getSenderEmails() {
		return senderEmails;
	}

	public void setSenderEmails(String senderEmails) {
		this.senderEmails = senderEmails;
	}

	@Editable(order=150, description="Specify space-separated projects authorized to senders above. "
			+ "Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to "
			+ "authorize all projects")
	@Patterns(suggester="suggestProjects")
	@NameOfEmptyValue("Any project")
	public String getAuthorizedProjects() {
		return authorizedProjects;
	}

	public void setAuthorizedProjects(String authorizedProjects) {
		this.authorizedProjects = authorizedProjects;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjects(matchWith);
	}
	
	@Editable(order=175, name="Authorized Role", description="Specify authorized role for above projects")
	@RoleChoice
	@NotEmpty
	public String getAuthorizedRoleName() {
		return authorizedRoleName;
	}

	public void setAuthorizedRoleName(String authorizedRoleName) {
		this.authorizedRoleName = authorizedRoleName;
	}
	
	public Role getAuthorizedRole() {
		Role role = OneDev.getInstance(RoleManager.class).find(authorizedRoleName);
		if (role == null)
			throw new ExplicitException("Undefined role: " + authorizedRoleName);
		return role;
	}
	
	public boolean isPermitted(Project project, Permission privilege) {
		String authorizedProjects = this.authorizedProjects;
		if (authorizedProjects == null)
			authorizedProjects = "*";
		Matcher matcher = new StringMatcher();
		return PatternSet.parse(authorizedProjects).matches(matcher, project.getName()) 
				&& getAuthorizedRole().implies(privilege);
	}
	
}