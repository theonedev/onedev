package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.match.PathMatcher;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.Usage;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.util.usermatcher.Anyone;
import io.onedev.server.util.usermatcher.UserMatcher;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.JobChoice;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
@Horizontal
@ClassValidating
public class BranchProtection implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String branches;
	
	private String user = new Anyone().toString();
	
	private boolean noForcedPush = true;
	
	private boolean noDeletion = true;
	
	private boolean noCreation = true;
	
	private String reviewRequirement;
	
	private List<String> jobNames = new ArrayList<>();
	
	private List<FileProtection> fileProtections = new ArrayList<>();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=100, description="Specify space-separated branches to be protected. Use * or ? for wildcard match")
	@Patterns(suggester = "suggestBranches")
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
	@io.onedev.server.web.editable.annotation.UserMatcher
	@NotEmpty(message="may not be empty")
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Editable(order=200, description="Check this to not allow forced push")
	public boolean isNoForcedPush() {
		return noForcedPush;
	}

	public void setNoForcedPush(boolean noForcedPush) {
		this.noForcedPush = noForcedPush;
	}

	@Editable(order=300, description="Check this to not allow branch deletion")
	public boolean isNoDeletion() {
		return noDeletion;
	}

	public void setNoDeletion(boolean noDeletion) {
		this.noDeletion = noDeletion;
	}

	@Editable(order=350, description="Check this to not allow branch creation")
	public boolean isNoCreation() {
		return noCreation;
	}

	public void setNoCreation(boolean noCreation) {
		this.noCreation = noCreation;
	}

	@Editable(order=400, name="Required Reviewers", description="Optionally specify required reviewers for changes of "
			+ "specified branch. OneDev assumes that the user submitting the change has completed the review already")
	@io.onedev.server.web.editable.annotation.ReviewRequirement
	@NameOfEmptyValue("No one")
	public String getReviewRequirement() {
		return reviewRequirement;
	}

	public void setReviewRequirement(String reviewRequirement) {
		this.reviewRequirement = reviewRequirement;
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
	
	@Editable(order=700, description="Optionally specify additional users to review "
			+ "particular paths. For each changed file, the first matched file protection setting will be used")
	@NotNull(message="may not be empty")
	@Valid
	public List<FileProtection> getFileProtections() {
		return fileProtections;
	}

	public void setFileProtections(List<FileProtection> fileProtections) {
		this.fileProtections = fileProtections;
	}
	
	@Nullable
	public FileProtection getFileProtection(String file) {
		for (FileProtection protection: fileProtections) {
			if (PatternSet.fromString(protection.getPaths()).matches(new PathMatcher(), file))
				return protection;
		}
		return null;
	}
	
	public void onRenameGroup(String oldName, String newName) {
		user = UserMatcher.onRenameGroup(user, oldName, newName);
		reviewRequirement = ReviewRequirement.onRenameGroup(reviewRequirement, oldName, newName);
		
		for (FileProtection fileProtection: getFileProtections()) {
			fileProtection.setReviewRequirement(ReviewRequirement.onRenameGroup(
					fileProtection.getReviewRequirement(), oldName, newName));
		}
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (UserMatcher.isUsingGroup(user, groupName))
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
	
	public void onRenameUser(Project project, String oldName, String newName) {
		user = UserMatcher.onRenameUser(user, oldName, newName);
		reviewRequirement = ReviewRequirement.onRenameUser(reviewRequirement, oldName, newName);
		
		for (FileProtection fileProtection: getFileProtections()) {
			fileProtection.setReviewRequirement(ReviewRequirement.onRenameUser(
					fileProtection.getReviewRequirement(), oldName, newName));
		}	
	}
	
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (UserMatcher.isUsingUser(user, userName))
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
	public boolean isReviewRequiredForModification(User user, Project project, String branch, @Nullable String file) {
		if (!ReviewRequirement.fromString(getReviewRequirement()).satisfied(user)) 
			return true;
		if (file != null) {
			FileProtection fileProtection = getFileProtection(file);
			if (fileProtection != null 
					&& !ReviewRequirement.fromString(fileProtection.getReviewRequirement()).satisfied(user)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isBuildRequiredForModification(Project project, String branch, @Nullable String file) {
		if (!getJobNames().isEmpty()) 
			return true;
		if (file != null) {
			FileProtection fileProtection = getFileProtection(file);
			if (fileProtection != null && !fileProtection.getJobNames().isEmpty()) 
				return true;
		}
		return false;
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
		if (!ReviewRequirement.fromString(getReviewRequirement()).satisfied(user)) 
			return true;

		for (String changedFile: project.getChangedFiles(oldObjectId, newObjectId, gitEnvs)) {
			FileProtection fileProtection = getFileProtection(changedFile);
			if (fileProtection != null  
					&& !ReviewRequirement.fromString(fileProtection.getReviewRequirement()).satisfied(user)) {
				return true;
			}
		}
		return false;
	}

	public Collection<String> getRequiredJobs(Project project, String branch, 
			ObjectId oldObjectId, ObjectId newObjectId, Map<String, String> gitEnvs) {
		Collection<String> requiredJobs = new HashSet<>(getJobNames());
		for (String changedFile: project.getChangedFiles(oldObjectId, newObjectId, gitEnvs)) {
			FileProtection fileProtection = getFileProtection(changedFile);
			if (fileProtection != null) 
				requiredJobs.addAll(fileProtection.getJobNames());
		}
		return requiredJobs;
	}
	
	public boolean isBuildRequiredForPush(Project project, String branch, ObjectId oldObjectId, 
			ObjectId newObjectId, Map<String, String> gitEnvs) {
		Collection<String> requiredJobNames = getRequiredJobs(project, branch, oldObjectId, newObjectId, gitEnvs);

		Collection<Build> builds = OneDev.getInstance(BuildManager.class).query(project, newObjectId);
		for (Build build: builds) {
			if (requiredJobNames.contains(build.getJobName()) && build.getStatus() != Status.SUCCESSFUL)
				return true;
		}
		requiredJobNames.removeAll(builds.stream().map(it->it.getJobName()).collect(Collectors.toSet()));
		return !requiredJobNames.isEmpty();			
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		int index = 1;
		for (FileProtection fileProtection: getFileProtections()) {
			if (fileProtection.getJobNames().isEmpty() && fileProtection.getReviewRequirement() == null) {
				context.disableDefaultConstraintViolation();
				String message = "File protection # " + index + ": Either reviewer or required builds should be specified";
				context.buildConstraintViolationWithTemplate(message)
						.addPropertyNode("fileProtections")
						.addConstraintViolation();
				return false;
			} 
			index++;
		}
		return true;
	}
	
}
