package com.gitplex.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.server.model.Group;
import com.gitplex.server.model.User;
import com.gitplex.server.util.editable.annotation.BranchPattern;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.ReviewRequirementSpec;
import com.gitplex.server.util.editable.annotation.VerificationChoice;
import com.gitplex.server.util.reviewrequirement.ReviewRequirement;
import com.gitplex.utils.PathUtils;

@Editable
public class BranchProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private String branch;
	
	private boolean noForcedPush = true;
	
	private boolean noDeletion = true;
	
	private String reviewRequirementSpec;
	
	private List<String> verifications = new ArrayList<>();
	
	private boolean verifyMerges;
	
	private transient Optional<ReviewRequirement> reviewRequirementOpt;
	
	private List<FileProtection> fileProtections = new ArrayList<>();

	@Editable(order=100, description="Specify branch to be protected. Wildcard may be used to "
			+ "specify multiple branches")
	@BranchPattern
	@NotEmpty
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
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

	@Editable(order=400, name="Required Reviewers", description="Optionally specify required reviewers for changes of "
			+ "specified branch. GitPlex assumes that the user submitting the change has completed the review already")
	@ReviewRequirementSpec
	public String getReviewRequirementSpec() {
		return reviewRequirementSpec;
	}

	public void setReviewRequirementSpec(String reviewRequirementSpec) {
		this.reviewRequirementSpec = reviewRequirementSpec;
	}

	@Editable(order=500, name="Required Verifications", description="Optionally choose required verifications. "
			+ "Verifications listed here are populated when external system (such as CI system) verifies and "
			+ "publishes commit statuses. Run relevant external system against this project if you can not "
			+ "find desired verifications here"
			+ "")
	@VerificationChoice
	public List<String> getVerifications() {
		return verifications;
	}

	public void setVerifications(List<String> verifications) {
		this.verifications = verifications;
	}

	@Editable(order=600, name="Verify Merged Commits", description="If checked, verifications specified above "
			+ "will be performed against merged commit (instead of head commit) when pull request is involved")
	public boolean isVerifyMerges() {
		return verifyMerges;
	}

	public void setVerifyMerges(boolean verifyMerges) {
		this.verifyMerges = verifyMerges;
	}

	@Editable(order=700, description="Optionally specify additional users to review particular paths. For each changed file, "
			+ "the first matched file protection setting will be used")
	@NotNull
	public List<FileProtection> getFileProtections() {
		return fileProtections;
	}

	public void setFileProtections(List<FileProtection> fileProtections) {
		this.fileProtections = fileProtections;
	}
	
	@Nullable
	public FileProtection getFileProtection(String file) {
		for (FileProtection protection: fileProtections) {
			if (PathUtils.matchChildAware(protection.getPath(), file))
				return protection;
		}
		return null;
	}
	
	@Nullable
	public ReviewRequirement getReviewRequirement() {
		if (reviewRequirementOpt == null) {
			if (reviewRequirementSpec != null)
				reviewRequirementOpt = Optional.of(new ReviewRequirement(reviewRequirementSpec));
			else
				reviewRequirementOpt = Optional.empty();
		}
		return reviewRequirementOpt.orElse(null);
	}
	
	public void onGroupRename(String oldName, String newName) {
		ReviewRequirement reviewRequirement = getReviewRequirement();
		if (reviewRequirement != null) {
			for (Group group: reviewRequirement.getGroups().keySet()) {
				if (group.getName().equals(oldName))
					group.setName(newName);
			}
			setReviewRequirementSpec(reviewRequirement.toSpec());
		}
		
		for (Iterator<FileProtection> it = getFileProtections().iterator(); it.hasNext();) {
			FileProtection fileProtection = it.next();
			reviewRequirement = fileProtection.getReviewRequirement();
			if (reviewRequirement != null) {
				for (Group group: reviewRequirement.getGroups().keySet()) {
					if (group.getName().equals(oldName))
						group.setName(newName);
				}
				fileProtection.setReviewRequirementSpec(reviewRequirement.toSpec());
			} else {
				it.remove();
			}
		}
	}
	
	public void onGroupDelete(String groupName) {
		ReviewRequirement reviewRequirement = getReviewRequirement();
		if (reviewRequirement != null) {
			for (Iterator<Map.Entry<Group, Integer>> it = reviewRequirement.getGroups().entrySet().iterator(); 
					it.hasNext();) {
				Group group = it.next().getKey();
				if (group.getName().equals(groupName))
					it.remove();
			}
			setReviewRequirementSpec(reviewRequirement.toSpec());
		}
		
		for (Iterator<FileProtection> it = getFileProtections().iterator(); it.hasNext();) {
			FileProtection fileProtection = it.next();
			reviewRequirement = fileProtection.getReviewRequirement();
			if (reviewRequirement != null) {
				for (Iterator<Map.Entry<Group, Integer>> itGroup = reviewRequirement.getGroups().entrySet().iterator(); 
						itGroup.hasNext();) {
					Group group = itGroup.next().getKey();
					if (group.getName().equals(groupName))
						itGroup.remove();
				}
				String reviewRequirementSpec = reviewRequirement.toSpec();
				if (reviewRequirementSpec != null)
					fileProtection.setReviewRequirementSpec(reviewRequirementSpec);
				else
					it.remove();
			} else {
				it.remove();
			}
		}
	}
	
	public void onUserRename(String oldName, String newName) {
		ReviewRequirement reviewRequirement = getReviewRequirement();
		if (reviewRequirement != null) {
			for (User user: reviewRequirement.getUsers()) {
				if (user.getName().equals(oldName))
					user.setName(newName);
			}
			setReviewRequirementSpec(reviewRequirement.toSpec());
		}
		
		for (Iterator<FileProtection> it = getFileProtections().iterator(); it.hasNext();) {
			FileProtection fileProtection = it.next();
			reviewRequirement = fileProtection.getReviewRequirement();
			if (reviewRequirement != null) {
				for (User user: reviewRequirement.getUsers()) {
					if (user.getName().equals(oldName))
						user.setName(newName);
				}
				fileProtection.setReviewRequirementSpec(reviewRequirement.toSpec());
			} else {
				it.remove();
			}
		}		
	}
	
	public void onUserDelete(String userName) {
		ReviewRequirement reviewRequirement = getReviewRequirement();
		if (reviewRequirement != null) {
			for (Iterator<User> it = reviewRequirement.getUsers().iterator(); it.hasNext();) {
				User user = it.next();
				if (user.getName().equals(userName))
					it.remove();
			}
			setReviewRequirementSpec(reviewRequirement.toSpec());
		}
		
		for (Iterator<FileProtection> it = getFileProtections().iterator(); it.hasNext();) {
			FileProtection fileProtection = it.next();
			reviewRequirement = fileProtection.getReviewRequirement();
			if (reviewRequirement != null) {
				for (Iterator<User> itUser = reviewRequirement.getUsers().iterator(); itUser.hasNext();) {
					User user = itUser.next();
					if (user.getName().equals(userName))
						itUser.remove();
				}
				String reviewRequirementSpec = reviewRequirement.toSpec();
				if (reviewRequirementSpec != null)
					fileProtection.setReviewRequirementSpec(reviewRequirementSpec);
				else
					it.remove();
			} else {
				it.remove();
			}
		}
	}
	
	public boolean onBranchDelete(String branchName) {
		return branchName.equals(getBranch());
	}

}
