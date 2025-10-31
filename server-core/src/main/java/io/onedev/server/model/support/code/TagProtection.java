package io.onedev.server.model.support.code;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.util.usermatch.Anyone;
import io.onedev.server.util.usermatch.UserMatch;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class TagProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String tags;
	
	private String userMatch = new Anyone().toString();
	
	private boolean preventUpdate = true;
	
	private boolean preventDeletion = true;
	
	private boolean preventCreation = true;
	
	private boolean commitSignatureRequired;

	private List<String> disallowedFileTypes = new ArrayList<>();
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=100, description="Specify space-separated tags to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude")
	@Patterns(suggester = "suggestTags", path=true)
	@NotEmpty
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestTags(String matchWith) {
		return SuggestionUtils.suggestTags(Project.get(), matchWith);
	}
	
	@Editable(order=150, name="Applicable Users", description="Rule will apply if user operating the tag matches criteria specified here")
	@io.onedev.server.annotation.UserMatch
	@NotEmpty(message="may not be empty")
	public String getUserMatch() {
		return userMatch;
	}

	public void setUserMatch(String userMatch) {
		this.userMatch = userMatch;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggestBranches(project, matchWith);
		else
			return new ArrayList<>();
	}
	
	@Editable(order=200, description="Check this to prevent tag update")
	public boolean isPreventUpdate() {
		return preventUpdate;
	}

	public void setPreventUpdate(boolean preventUpdate) {
		this.preventUpdate = preventUpdate;
	}

	@Editable(order=300, description="Check this to prevent tag deletion")
	public boolean isPreventDeletion() {
		return preventDeletion;
	}

	public void setPreventDeletion(boolean preventDeletion) {
		this.preventDeletion = preventDeletion;
	}

	@Editable(order=400, description="Check this to prevent tag creation")
	public boolean isPreventCreation() {
		return preventCreation;
	}

	public void setPreventCreation(boolean preventCreation) {
		this.preventCreation = preventCreation;
	}

	@Editable(order=560, description="Check this to require valid signature of head commit")
	public boolean isCommitSignatureRequired() {
		return commitSignatureRequired;
	}

	public void setCommitSignatureRequired(boolean commitSignatureRequired) {
		this.commitSignatureRequired = commitSignatureRequired;
	}

	@Editable(order=410, placeholder = "No disallowed file types", description = "Optionally specify disallowed file types by extensions (hit ENTER to add value), for instance <code>exe</code>, <code>bin</code>. Leave empty to allow all file types")
	public List<String> getDisallowedFileTypes() {
		return disallowedFileTypes;
	}

	public void setDisallowedFileTypes(List<String> disallowedFileTypes) {
		this.disallowedFileTypes = disallowedFileTypes;
	}

	public void onRenameGroup(String oldName, String newName) {
		userMatch = UserMatch.onRenameGroup(userMatch, oldName, newName);
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (UserMatch.isUsingGroup(userMatch, groupName))
			usage.add("applicable users");
		return usage.prefix("tag protection '" + getTags() + "'").prefix("code");
	}
	
	public void onRenameUser(String oldName, String newName) {
		userMatch = UserMatch.onRenameUser(userMatch, oldName, newName);
	}
	
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (UserMatch.isUsingUser(userMatch, userName))
			usage.add("applicable users");
		return usage.prefix("tag protection '" + getTags() + "'").prefix("code");
	}

	public Usage getTagUsage(String tagName) {
		Usage usage = new Usage();
		PatternSet patternSet = PatternSet.parse(getTags());
		if (patternSet.getIncludes().contains(tagName) || patternSet.getExcludes().contains(tagName))
			usage.add("tag protection '" + getTags() + "'");
		return usage;
	}
	
	public Collection<String> getViolatedFileTypes(Project project, ObjectId objectId, Map<String, String> gitEnvs) {
		if (disallowedFileTypes.isEmpty()) {
			return Collections.emptySet();
		} else {
			var files = getGitService().getChangedFiles(project, ObjectId.zeroId(), objectId, gitEnvs);
			return getDisallowedFileTypes().stream()
				.filter(type -> files.stream().anyMatch(file -> file.toLowerCase().endsWith("." + type.toLowerCase())))
				.collect(Collectors.toSet());
		}
	}

	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}

}
