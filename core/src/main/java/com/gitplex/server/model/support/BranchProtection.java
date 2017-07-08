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

import com.gitplex.server.model.User;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.Group;
import com.gitplex.server.util.PathUtils;
import com.gitplex.server.util.editable.annotation.BranchPattern;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.VerificationChoice;
import com.gitplex.server.util.reviewappointment.ReviewAppointment;

@Editable
public class BranchProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private String branch;
	
	private boolean noForcedPush = true;
	
	private boolean noDeletion = true;
	
	private String reviewAppointmentExpr;
	
	private List<String> verifications = new ArrayList<>();
	
	private boolean verifyMerges;
	
	private transient Optional<ReviewAppointment> reviewAppointmentOpt;
	
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
			+ "specified branch. Note that the user submitting the change is considered to reviewed the change "
			+ "automatically")
	@com.gitplex.server.util.editable.annotation.ReviewAppointment
	public String getReviewAppointmentExpr() {
		return reviewAppointmentExpr;
	}

	public void setReviewAppointmentExpr(String reviewAppointmentExpr) {
		this.reviewAppointmentExpr = reviewAppointmentExpr;
	}

	@Editable(order=500, name="Required Verifications", description="Optionally specify required verifications")
	@VerificationChoice
	public List<String> getVerifications() {
		return verifications;
	}

	public void setVerifications(List<String> verifications) {
		this.verifications = verifications;
	}

	@Editable(order=600, name="Verify Merged Commit", description="For required verifications specified above, this option determines whether or "
			+ "not to verify merged commits of relevant pull requests")
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
	public ReviewAppointment getReviewAppointment(Project project) {
		if (reviewAppointmentOpt == null) {
			if (reviewAppointmentExpr != null)
				reviewAppointmentOpt = Optional.of(new ReviewAppointment(project, reviewAppointmentExpr));
			else
				reviewAppointmentOpt = Optional.empty();
		}
		return reviewAppointmentOpt.orElse(null);
	}
	
	public void onGroupRename(Project project, String oldName, String newName) {
		ReviewAppointment reviewAppointment = getReviewAppointment(project);
		if (reviewAppointment != null) {
			for (Group group: reviewAppointment.getGroups().keySet()) {
				if (group.getName().equals(oldName))
					group.setName(newName);
			}
			setReviewAppointmentExpr(reviewAppointment.toExpr());
		}
		
		for (Iterator<FileProtection> it = getFileProtections().iterator(); it.hasNext();) {
			FileProtection fileProtection = it.next();
			reviewAppointment = fileProtection.getReviewAppointment(project);
			if (reviewAppointment != null) {
				for (Group group: reviewAppointment.getGroups().keySet()) {
					if (group.getName().equals(oldName))
						group.setName(newName);
				}
				fileProtection.setReviewAppointmentExpr(reviewAppointment.toExpr());
			} else {
				it.remove();
			}
		}
	}
	
	public void onGroupDelete(Project project, String groupName) {
		ReviewAppointment reviewAppointment = getReviewAppointment(project);
		if (reviewAppointment != null) {
			for (Iterator<Map.Entry<Group, Integer>> it = reviewAppointment.getGroups().entrySet().iterator(); 
					it.hasNext();) {
				Group group = it.next().getKey();
				if (group.getName().equals(groupName))
					it.remove();
			}
			setReviewAppointmentExpr(reviewAppointment.toExpr());
		}
		
		for (Iterator<FileProtection> it = getFileProtections().iterator(); it.hasNext();) {
			FileProtection fileProtection = it.next();
			reviewAppointment = fileProtection.getReviewAppointment(project);
			if (reviewAppointment != null) {
				for (Iterator<Map.Entry<Group, Integer>> itGroup = reviewAppointment.getGroups().entrySet().iterator(); 
						itGroup.hasNext();) {
					Group group = itGroup.next().getKey();
					if (group.getName().equals(groupName))
						itGroup.remove();
				}
				String reviewAppointmentExpr = reviewAppointment.toExpr();
				if (reviewAppointmentExpr != null)
					fileProtection.setReviewAppointmentExpr(reviewAppointmentExpr);
				else
					it.remove();
			} else {
				it.remove();
			}
		}
	}
	
	public void onUserRename(Project project, String oldName, String newName) {
		ReviewAppointment reviewAppointment = getReviewAppointment(project);
		if (reviewAppointment != null) {
			for (User user: reviewAppointment.getUsers()) {
				if (user.getName().equals(oldName))
					user.setName(newName);
			}
			setReviewAppointmentExpr(reviewAppointment.toExpr());
		}
		
		for (Iterator<FileProtection> it = getFileProtections().iterator(); it.hasNext();) {
			FileProtection fileProtection = it.next();
			reviewAppointment = fileProtection.getReviewAppointment(project);
			if (reviewAppointment != null) {
				for (User user: reviewAppointment.getUsers()) {
					if (user.getName().equals(oldName))
						user.setName(newName);
				}
				fileProtection.setReviewAppointmentExpr(reviewAppointment.toExpr());
			} else {
				it.remove();
			}
		}		
	}
	
	public void onUserDelete(Project project, String userName) {
		ReviewAppointment reviewAppointment = getReviewAppointment(project);
		if (reviewAppointment != null) {
			for (Iterator<User> it = reviewAppointment.getUsers().iterator(); it.hasNext();) {
				User user = it.next();
				if (user.getName().equals(userName))
					it.remove();
			}
			setReviewAppointmentExpr(reviewAppointment.toExpr());
		}
		
		for (Iterator<FileProtection> it = getFileProtections().iterator(); it.hasNext();) {
			FileProtection fileProtection = it.next();
			reviewAppointment = fileProtection.getReviewAppointment(project);
			if (reviewAppointment != null) {
				for (Iterator<User> itUser = reviewAppointment.getUsers().iterator(); itUser.hasNext();) {
					User user = itUser.next();
					if (user.getName().equals(userName))
						itUser.remove();
				}
				String reviewAppointmentExpr = reviewAppointment.toExpr();
				if (reviewAppointmentExpr != null)
					fileProtection.setReviewAppointmentExpr(reviewAppointmentExpr);
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
