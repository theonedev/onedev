package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.Valid;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.util.usermatch.Anyone;
import io.onedev.server.util.usermatch.UserMatch;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.JobChoice;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
@Horizontal
public class BranchProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String branches;
	
	private String userMatch = new Anyone().toString();
	
	private boolean preventForcedPush = true;
	
	private boolean preventDeletion = true;
	
	private boolean preventCreation = true;
	
	private String reviewRequirement;
	
	private transient ReviewRequirement parsedReviewRequirement;
	
	private List<String> jobNames = new ArrayList<>();
	
	private List<FileProtection> fileProtections = new ArrayList<>();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=100, description="Specify space-separated branches to be protected. Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude")
	@Patterns(suggester = "suggestBranches", path=true)
	@NotEmpty
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		return SuggestionUtils.suggestBranches(Project.get(), matchWith);
	}
	
	@Editable(order=150, name="Applicable Users", description="Rule will apply only if the user changing the branch matches criteria specified here")
	@io.onedev.server.web.editable.annotation.UserMatch
	@NotEmpty(message="may not be empty")
	public String getUserMatch() {
		return userMatch;
	}

	public void setUserMatch(String userMatch) {
		this.userMatch = userMatch;
	}

	@Editable(order=200, description="Check this to prevent forced push")
	public boolean isPreventForcedPush() {
		return preventForcedPush;
	}

	public void setPreventForcedPush(boolean preventForcedPush) {
		this.preventForcedPush = preventForcedPush;
	}

	@Editable(order=300, description="Check this to prevent branch deletion")
	public boolean isPreventDeletion() {
		return preventDeletion;
	}

	public void setPreventDeletion(boolean preventDeletion) {
		this.preventDeletion = preventDeletion;
	}

	@Editable(order=350, description="Check this to prevent branch creation")
	public boolean isPreventCreation() {
		return preventCreation;
	}

	public void setPreventCreation(boolean preventCreation) {
		this.preventCreation = preventCreation;
	}

	@Editable(order=400, name="Required Reviewers", description="Optionally specify required reviewers for changes of "
			+ "specified branch")
	@io.onedev.server.web.editable.annotation.ReviewRequirement
	@NameOfEmptyValue("No one")
	public String getReviewRequirement() {
		return reviewRequirement;
	}

	public void setReviewRequirement(String reviewRequirement) {
		this.reviewRequirement = reviewRequirement;
	}

	public ReviewRequirement getParsedReviewRequirement() {
		if (parsedReviewRequirement == null)
			parsedReviewRequirement = ReviewRequirement.parse(reviewRequirement, true);
		return parsedReviewRequirement;
	}
	
	public void setParsedReviewRequirement(ReviewRequirement parsedReviewRequirement) {
		this.parsedReviewRequirement = parsedReviewRequirement;
		reviewRequirement = parsedReviewRequirement.toString();
	}
	
	@Editable(order=500, name="Required Builds", description="Optionally choose required builds")
	@JobChoice
	@NameOfEmptyValue("No any")
	public List<String> getJobNames() {
		return jobNames;
	}

	public void setJobNames(List<String> jobNames) {
		this.jobNames = jobNames;
	}
	
	@Editable(order=700, description="Optionally specify path protection rules")
	@Valid
	public List<FileProtection> getFileProtections() {
		return fileProtections;
	}

	public void setFileProtections(List<FileProtection> fileProtections) {
		this.fileProtections = fileProtections;
	}
	
	public FileProtection getFileProtection(String file) {
		Set<String> jobNames = new HashSet<>();
		ReviewRequirement reviewRequirement = ReviewRequirement.parse(null, true);
		for (FileProtection protection: fileProtections) {
			if (PatternSet.parse(protection.getPaths()).matches(new PathMatcher(), file)) {
				jobNames.addAll(protection.getJobNames());
				reviewRequirement.mergeWith(protection.getParsedReviewRequirement());
			}
		}
		FileProtection protection = new FileProtection();
		protection.setJobNames(new ArrayList<>(jobNames));
		protection.setParsedReviewRequirement(reviewRequirement);
		return protection;
	}
	
	public void onRenameGroup(String oldName, String newName) {
		userMatch = UserMatch.onRenameGroup(userMatch, oldName, newName);
		reviewRequirement = ReviewRequirement.onRenameGroup(reviewRequirement, oldName, newName);
		
		for (FileProtection fileProtection: getFileProtections()) {
			fileProtection.setReviewRequirement(ReviewRequirement.onRenameGroup(
					fileProtection.getReviewRequirement(), oldName, newName));
		}
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (UserMatch.isUsingGroup(userMatch, groupName))
			usage.add("applicable users");
		if (ReviewRequirement.isUsingGroup(reviewRequirement, groupName))
			usage.add("required reviewers");

		for (FileProtection protection: getFileProtections()) {
			if (ReviewRequirement.isUsingGroup(protection.getReviewRequirement(), groupName)) {
				usage.add("file protections");
				break;
			}
		}
		return usage.prefix("branch protection '" + getBranches() + "'");
	}
	
	public void onRenameUser(String oldName, String newName) {
		userMatch = UserMatch.onRenameUser(userMatch, oldName, newName);
		reviewRequirement = ReviewRequirement.onRenameUser(reviewRequirement, oldName, newName);
		
		for (FileProtection fileProtection: getFileProtections()) {
			fileProtection.setReviewRequirement(ReviewRequirement.onRenameUser(
					fileProtection.getReviewRequirement(), oldName, newName));
		}	
	}
	
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (UserMatch.isUsingUser(userMatch, userName))
			usage.add("applicable users");
		if (ReviewRequirement.isUsingUser(reviewRequirement, userName))
			usage.add("required reviewers");

		for (FileProtection protection: getFileProtections()) {
			if (ReviewRequirement.isUsingUser(protection.getReviewRequirement(), userName)) {
				usage.add("file protections");
				break;
			}
		}
		return usage.prefix("branch protection '" + getBranches() + "'");
	}
	
	/**
	 * Check if specified user can modify specified file in specified branch.
	 *
	 * @param user
	 * 			user to be checked
	 * @param branch
	 * 			branch to be checked
	 * @param file
	 * 			file to be checked
	 * @return
	 * 			result of the check. 
	 */
	public boolean isReviewRequiredForModification(User user, Project project, 
			String branch, @Nullable String file) {
		ReviewRequirement requirement = getParsedReviewRequirement();
		if (!requirement.getUsers().isEmpty() || !requirement.getGroups().isEmpty()) 
			return true;
		
		if (file != null) {
			requirement = getFileProtection(file).getParsedReviewRequirement();
			return !requirement.getUsers().isEmpty() || !requirement.getGroups().isEmpty();
		} 
		
		return false;
	}
	
	public boolean isBuildRequiredForModification(Project project, String branch, @Nullable String file) {
		return !getJobNames().isEmpty() || file != null && !getFileProtection(file).getJobNames().isEmpty();
	}

	/**
	 * Check if specified user can push specified commit to specified ref.
	 *
	 * @param user
	 * 			user to be checked
	 * @param branchName
	 * 			branchName to be checked
	 * @param oldObjectId
	 * 			old object id of the ref
	 * @param newObjectId
	 * 			new object id of the ref
	 * @param gitEnvs
	 * 			git environments
	 * @return
	 * 			result of the check
	 */
	public boolean isReviewRequiredForPush(User user, Project project, String branch, ObjectId oldObjectId, 
			ObjectId newObjectId, Map<String, String> gitEnvs) {
		ReviewRequirement requirement = getParsedReviewRequirement();
		if (!requirement.getUsers().isEmpty() || !requirement.getGroups().isEmpty()) 
			return true;
		
		for (String changedFile: project.getChangedFiles(oldObjectId, newObjectId, gitEnvs)) {
			requirement = getFileProtection(changedFile).getParsedReviewRequirement();
			if (!requirement.getUsers().isEmpty() || !requirement.getGroups().isEmpty())
				return true;
		}

		return false;
	}

	public Collection<String> getRequiredJobs(Project project, ObjectId oldObjectId, ObjectId newObjectId, 
			Map<String, String> gitEnvs) {
		Collection<String> requiredJobs = new HashSet<>(getJobNames());
		for (String changedFile: project.getChangedFiles(oldObjectId, newObjectId, gitEnvs)) 
			requiredJobs.addAll(getFileProtection(changedFile).getJobNames());
		return requiredJobs;
	}
	
	public boolean isBuildRequiredForPush(Project project, ObjectId oldObjectId, ObjectId newObjectId, 
			Map<String, String> gitEnvs) {
		Collection<String> requiredJobNames = getRequiredJobs(project, oldObjectId, newObjectId, gitEnvs);

		Collection<Build> builds = OneDev.getInstance(BuildManager.class).query(project, newObjectId);
		for (Build build: builds) {
			if (requiredJobNames.contains(build.getJobName()) && build.getStatus() != Status.SUCCESSFUL)
				return true;
		}
		requiredJobNames.removeAll(builds.stream().map(it->it.getJobName()).collect(Collectors.toSet()));
		return !requiredJobNames.isEmpty();			
	}

}
