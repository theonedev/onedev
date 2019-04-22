package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.utils.stringmatch.ChildAwareMatcher;
import io.onedev.server.model.Project;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.util.usermatcher.Anyone;
import io.onedev.server.util.usermatcher.UserMatcher;
import io.onedev.server.web.editable.annotation.BranchPatterns;
import io.onedev.server.web.editable.annotation.ConfigurationChoice;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class BranchProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String branches;
	
	private String submitter = new Anyone().toString();
	
	private boolean noForcedPush = true;
	
	private boolean noDeletion = true;
	
	private boolean noCreation = true;
	
	private String reviewRequirement;
	
	private List<String> configurations = new ArrayList<>();
	
	private boolean buildMerges;
	
	private List<FileProtection> fileProtections = new ArrayList<>();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=100, description="Specify space-separated branches to be protected. Use * or ? for wildcard match")
	@BranchPatterns
	@NotEmpty
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@Editable(order=150, name="If Submitted By", description="This protection rule will apply "
			+ "only if the change is submitted by specified users here")
	@io.onedev.server.web.editable.annotation.UserMatcher
	@NotEmpty(message="may not be empty")
	public String getSubmitter() {
		return submitter;
	}

	public void setSubmitter(String submitter) {
		this.submitter = submitter;
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
	@ConfigurationChoice
	@NameOfEmptyValue("No any")
	public List<String> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<String> configurations) {
		this.configurations = configurations;
	}

	@Editable(order=600, name="Build Merged Commits", description="If checked, builds of merged commits "
			+ "(instead of head commits) are required when pull request is involved")
	public boolean isBuildMerges() {
		return buildMerges;
	}

	public void setBuildMerges(boolean buildMerges) {
		this.buildMerges = buildMerges;
	}

	@Editable(order=700, description="Optionally specify additional users to review particular paths. For each changed file, "
			+ "the first matched file protection setting will be used")
	@NotNull(message="may not be empty")
	public List<FileProtection> getFileProtections() {
		return fileProtections;
	}

	public void setFileProtections(List<FileProtection> fileProtections) {
		this.fileProtections = fileProtections;
	}
	
	@Nullable
	public FileProtection getFileProtection(String file) {
		for (FileProtection protection: fileProtections) {
			if (PatternSet.fromString(protection.getPaths()).matches(new ChildAwareMatcher(), file))
				return protection;
		}
		return null;
	}
	
	public void onRenameGroup(String oldName, String newName) {
		submitter = UserMatcher.onRenameGroup(submitter, oldName, newName);
		reviewRequirement = ReviewRequirement.onRenameGroup(reviewRequirement, oldName, newName);
		
		for (FileProtection fileProtection: getFileProtections()) {
			fileProtection.setReviewRequirement(ReviewRequirement.onRenameGroup(
					fileProtection.getReviewRequirement(), oldName, newName));
		}
	}
	
	public boolean onDeleteGroup(String groupName) {
		submitter = UserMatcher.onDeleteGroup(submitter, groupName);
		reviewRequirement = ReviewRequirement.onDeleteGroup(reviewRequirement, groupName);

		for (Iterator<FileProtection> it = getFileProtections().iterator(); it.hasNext();) {
			FileProtection protection = it.next();
			String reviewRequirement = ReviewRequirement.onDeleteGroup(protection.getReviewRequirement(), groupName);
			if (reviewRequirement != null)
				protection.setReviewRequirement(reviewRequirement);
			else
				it.remove();
		}
		
		return false;
	}
	
	public void onRenameConfiguration(String oldName, String newName) {
		int index = getConfigurations().indexOf(oldName);
		if (index != -1)
			getConfigurations().set(index, newName);
	}
	
	public void onDeleteConfiguration(String configurationName) {
		getConfigurations().remove(configurationName);
	}
	
	public void onRenameUser(Project project, String oldName, String newName) {
		submitter = UserMatcher.onRenameUser(submitter, oldName, newName);
		reviewRequirement = ReviewRequirement.onRenameUser(reviewRequirement, oldName, newName);
		
		for (FileProtection fileProtection: getFileProtections()) {
			fileProtection.setReviewRequirement(ReviewRequirement.onRenameUser(
					fileProtection.getReviewRequirement(), oldName, newName));
		}	
	}
	
	public boolean onDeleteUser(Project project, String userName) {
		submitter = UserMatcher.onDeleteUser(submitter, userName);
		reviewRequirement = ReviewRequirement.onDeleteUser(reviewRequirement, userName);

		for (Iterator<FileProtection> it = getFileProtections().iterator(); it.hasNext();) {
			FileProtection protection = it.next();
			String reviewRequirement = ReviewRequirement.onDeleteUser(protection.getReviewRequirement(), userName);
			if (reviewRequirement != null)
				protection.setReviewRequirement(reviewRequirement);
			else
				it.remove();
		}
		return false;
	}
	
	public boolean onBranchDeleted(String branchName) {
		PatternSet patternSet = PatternSet.fromString(getBranches());
		patternSet.getIncludes().remove(branchName);
		patternSet.getExcludes().remove(branchName);
		setBranches(patternSet.toString());
		return getBranches().length() == 0;
	}

}
