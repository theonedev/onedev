package com.gitplex.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.Permission;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.manager.VerificationManager;
import com.gitplex.server.model.Group;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.model.Review;
import com.gitplex.server.model.ReviewInvitation;
import com.gitplex.server.model.User;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.FileProtection;
import com.gitplex.server.model.support.MergePreview;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.security.permission.ProjectPermission;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserFacade;
import com.gitplex.server.util.reviewrequirement.ReviewRequirement;

public class QualityCheckStatus {

	private static final int MAX_CONTRIBUTION_FILES = 100;
	
	private final PullRequest request;
	
	private final List<User> awaitingReviewers = new ArrayList<>();
	
	private final Map<User, Review> effectiveReviews = new HashMap<>();
	
	private final List<String> awaitingVerifications = new ArrayList<>();
	
	private final Map<String, Verification> effectiveVerifications = new HashMap<>();
	
	public List<User> getAwaitingReviewers() {
		return awaitingReviewers;
	}

	public Map<User, Review> getEffectiveReviews() {
		return effectiveReviews;
	}

	@Nullable
	private ReviewInvitation getInvitation(Long userId) {
		for (ReviewInvitation invitation: request.getReviewInvitations()) {
			if (invitation.getUser().getId().equals(userId))
				return invitation;
		}
		return null;
	}
	
	@Nullable
	private Review getReviewAfter(Long userId, PullRequestUpdate update) {
		for (Review review: update.getRequest().getReviews()) {
			if (review.getUser().getId().equals(userId) && review.getUpdate() != null 
					&& review.getUpdate().getId()>=update.getId()) {
				return review;
			}
		}
		return null;
	}
	
	public QualityCheckStatus(PullRequest request) {
		this.request = request;
		
		for (ReviewInvitation invitation: request.getReviewInvitations()) {
			if (!invitation.getUser().equals(request.getSubmitter()) 
					&& invitation.getType() == ReviewInvitation.Type.MANUAL) {
				UserFacade user = invitation.getUser().getFacade();
				Review effectiveReview = getReviewAfter(user.getId(), request.getLatestUpdate());
				if (effectiveReview == null) 
					awaitingReviewers.add(invitation.getUser());
				else
					effectiveReviews.put(invitation.getUser(), effectiveReview);
			}
		}

		BranchProtection branchProtection = request.getTargetProject().getBranchProtection(request.getTargetBranch());
		if (branchProtection != null) {
			ReviewRequirement reviewRequirement = branchProtection.getReviewRequirement();
			if (reviewRequirement != null) 
				checkReviews(reviewRequirement, request.getLatestUpdate());

			if (branchProtection.getVerifications() != null)
				checkVerifications(branchProtection.getVerifications(), branchProtection.isVerifyMerges());
			
			Set<FileProtection> checkedFileProtections = new HashSet<>();
			for (int i=request.getSortedUpdates().size()-1; i>=0; i--) {
				if (checkedFileProtections.containsAll(branchProtection.getFileProtections()))
					break;
				PullRequestUpdate update = request.getSortedUpdates().get(i);
				for (String file: update.getChangedFiles()) {
					FileProtection fileProtection = branchProtection.getFileProtection(file);
					if (fileProtection != null && !checkedFileProtections.contains(fileProtection)) {
						checkedFileProtections.add(fileProtection);
						checkReviews(fileProtection.getReviewRequirement(), update);
					}
				}
			}
		}
		
		ProjectFacade project = request.getTargetProject().getFacade();
		Permission writePermission = new ProjectPermission(project, ProjectPrivilege.WRITE); 
		if (request.getSubmitter() == null || !request.getSubmitter().asSubject().isPermitted(writePermission)) {
			Collection<UserFacade> writers = SecurityUtils.getAuthorizedUsers(project, ProjectPrivilege.WRITE);
			checkReviews(writers, 1, request.getLatestUpdate());
		}
		
	}
	
	private boolean isAwaitingReviewer(Long userId) {
		for (User user: awaitingReviewers) {
			if (user.getId().equals(userId))
				return true;
		}
		return false;
	}
	
	private void checkVerifications(List<String> verificationNames, boolean verifyMerges) {
		String commit;
		if (verifyMerges) {
			MergePreview preview = request.getMergePreview();
			if (preview != null && preview.getMerged() != null) {
				commit = preview.getMerged();
			} else {
				commit = null;
			}
		} else {
			commit = request.getHeadCommitHash();			
		}
		
		Map<String, Verification> verifications;
		if (commit != null) {
			verifications = GitPlex.getInstance(VerificationManager.class)
					.getVerifications(request.getTargetProject(), commit);
		} else {
			verifications = new HashMap<>();
		}
		for (String verificationName: verificationNames) {
			Verification verification = verifications.get(verificationName);
			if (verification != null) 
				effectiveVerifications.put(verificationName, verification);
			else
				awaitingVerifications.add(verificationName);
		}
	}
	
	private void checkReviews(ReviewRequirement reviewRequirement, PullRequestUpdate update) {
		for (User user: reviewRequirement.getUsers()) {
			if (!user.equals(request.getSubmitter()) && !isAwaitingReviewer(user.getId())) {
				Review effectiveReview = getReviewAfter(user.getId(), update);
				if (effectiveReview == null) {
					awaitingReviewers.add(user);
					inviteReviewer(user.getId());
				} else {
					effectiveReviews.put(user, effectiveReview);
				}
			}
		}
		
		for (Map.Entry<Group, Integer> entry: reviewRequirement.getGroups().entrySet()) {
			Group group = entry.getKey();
			int requiredCount = entry.getValue();
			checkReviews(getFacades(group.getMembers()), requiredCount, update);
		}
	}
	
	private Collection<UserFacade> getFacades(Collection<User> users) {
		Collection<UserFacade> facades = new HashSet<>();
		for (User user: users)
			facades.add(user.getFacade());
		return facades;
	}
	
	private void checkReviews(Collection<UserFacade> users, int requiredCount, PullRequestUpdate update) {
		if (requiredCount == 0)
			requiredCount = users.size();
		
		int effectiveCount = 0;
		Set<UserFacade> potentialReviewers = new HashSet<>();
		for (UserFacade user: users) {
			if (request.getSubmitter() != null && user.getId().equals(request.getSubmitter().getId())) {
				effectiveCount++;
			} else if (isAwaitingReviewer(user.getId())) {
				requiredCount--;
			} else {
				Review effectiveReview = getReviewAfter(user.getId(), update);
				if (effectiveReview != null) {
					effectiveReviews.put(GitPlex.getInstance(UserManager.class).load(user.getId()), effectiveReview);					
					effectiveCount++;
				} else {
					potentialReviewers.add(user);
				}
			}
			if (effectiveCount >= requiredCount)
				break;
		}

		int missingCount = requiredCount - effectiveCount;
		Set<UserFacade> reviewers = new HashSet<>();
		
		List<UserFacade> candidateReviewers = new ArrayList<>();
		for (UserFacade user: potentialReviewers) {
			ReviewInvitation invitation = getInvitation(user.getId());
			if (invitation != null && invitation.getType() != ReviewInvitation.Type.EXCLUDE)
				candidateReviewers.add(user);
		}
		
		sortByContributions(candidateReviewers, update);
		for (UserFacade user: candidateReviewers) {
			reviewers.add(user);
			if (reviewers.size() == missingCount)
				break;
		}
		
		if (reviewers.size() < missingCount) {
			candidateReviewers = new ArrayList<>();
			for (UserFacade user: potentialReviewers) {
				ReviewInvitation invitation = getInvitation(user.getId());
				if (invitation == null) 
					candidateReviewers.add(user);
			}
			sortByContributions(candidateReviewers, update);
			for (UserFacade user: candidateReviewers) {
				reviewers.add(user);
				if (reviewers.size() == missingCount)
					break;
			}
		}
		if (reviewers.size() < missingCount) {
			List<ReviewInvitation> excludedInvitations = new ArrayList<>();
			for (UserFacade user: potentialReviewers) {
				ReviewInvitation invitation = getInvitation(user.getId());
				if (invitation != null && invitation.getType() == ReviewInvitation.Type.EXCLUDE)
					excludedInvitations.add(invitation);
			}
			excludedInvitations.sort(Comparator.comparing(ReviewInvitation::getDate));
			
			for (ReviewInvitation invitation: excludedInvitations) {
				reviewers.add(invitation.getUser().getFacade());
				if (reviewers.size() == missingCount)
					break;
			}
		}
		if (reviewers.size() < missingCount) {
			String errorMessage = String.format("Impossible to provide required number of reviewers "
					+ "(candidates: %s, required number of reviewers: %d, pull request: #%d)", 
					reviewers, missingCount, update.getRequest().getNumber());
			throw new RuntimeException(errorMessage);
		}

		UserManager userManager = GitPlex.getInstance(UserManager.class);
		for (UserFacade user: reviewers) {
			awaitingReviewers.add(userManager.load(user.getId()));
			inviteReviewer(user.getId());
		}
	}
	
	private void sortByContributions(List<UserFacade> users, PullRequestUpdate update) {
		CommitInfoManager commitInfoManager = GitPlex.getInstance(CommitInfoManager.class);
		Map<UserFacade, Integer> contributions = new HashMap<>();
		for (UserFacade user: users)
			contributions.put(user, 0);

		int count = 0;
		for (String file: update.getChangedFiles()) {
			int addedContributions = addContributions(contributions, commitInfoManager, file, update);
			while (addedContributions == 0) {
				if (file.contains("/")) {
					file = StringUtils.substringBeforeLast(file, "/");
					addedContributions = addContributions(contributions, commitInfoManager, file, update);
				} else {
					addContributions(contributions, commitInfoManager, "", update);
					break;
				}
			}
			if (++count >= MAX_CONTRIBUTION_FILES)
				break;
		}

		users.sort((user1, user2)-> contributions.get(user2) - contributions.get(user1));
	}
	
	private int addContributions(Map<UserFacade, Integer> contributions, CommitInfoManager commitInfoManager, 
			String file, PullRequestUpdate update) {
		int addedContributions = 0;
		for (Map.Entry<UserFacade, Integer> entry: contributions.entrySet()) {
			UserFacade user = entry.getKey();
			int fileContribution = commitInfoManager.getContributions(
					update.getRequest().getTargetProject().getFacade(), user, file);
			entry.setValue(entry.getValue() + fileContribution);
			addedContributions += fileContribution;
		}
		return addedContributions;
	}
	
	private void inviteReviewer(Long userId) {
		ReviewInvitation invitation = getInvitation(userId);
		if (invitation != null) {
			invitation.setType(ReviewInvitation.Type.RULE);
		} else {
			invitation = new ReviewInvitation();
			invitation.setRequest(request);
			invitation.setUser(GitPlex.getInstance(UserManager.class).load(userId));
			invitation.setType(ReviewInvitation.Type.RULE);
			request.getReviewInvitations().add(invitation);
		}
	}

	public List<String> getAwaitingVerifications() {
		return awaitingVerifications;
	}

	public Map<String, Verification> getEffectiveVerifications() {
		return effectiveVerifications;
	}
	
}