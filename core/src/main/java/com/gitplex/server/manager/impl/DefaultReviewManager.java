package com.gitplex.server.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.manager.ReviewManager;
import com.gitplex.server.manager.PullRequestStatusChangeManager;
import com.gitplex.server.model.User;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.Review;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.PullRequestStatusChange.Type;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.model.ReviewInvitation;
import com.gitplex.server.model.Group;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.FileProtection;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.security.permission.ProjectPermission;
import com.gitplex.server.util.ReviewStatus;
import com.gitplex.server.util.reviewappointment.ReviewAppointment;

@Singleton
public class DefaultReviewManager extends AbstractEntityManager<Review> 
		implements ReviewManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultReviewManager.class);
	
	private static final int MAX_CONTRIBUTION_FILES = 100;
	
	private final UserManager userManager;

	private final PullRequestManager pullRequestManager;
	
	private final PullRequestStatusChangeManager pullRequestStatusChangeManager;
	
	@Inject
	public DefaultReviewManager(Dao dao, UserManager userManager, 
			PullRequestManager pullRequestManager, PullRequestStatusChangeManager pullRequestStatusChangeManager) {
		super(dao);
		
		this.userManager = userManager;
		this.pullRequestManager = pullRequestManager;
		this.pullRequestStatusChangeManager = pullRequestStatusChangeManager;
	}

	@Transactional
	@Override
	public ReviewStatus checkRequest(PullRequest request) {
		return new ReviewStatusImpl(request);
	}

	@Transactional
	@Override
	public void save(Review review) {
		super.save(review);	

		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(new Date());
		statusChange.setNote(review.getNote());
		statusChange.setRequest(review.getRequest());
		if (review.isApproved()) {
			statusChange.setType(Type.APPROVED);
		} else {
			statusChange.setType(Type.DISAPPROVED);
		}
		statusChange.setUser(review.getUser());
		pullRequestStatusChangeManager.save(statusChange);
		
		review.getRequest().setLastEvent(statusChange);
		pullRequestManager.save(review.getRequest());
	}

	@Sessional
	@Override
	public List<Review> findAll(PullRequest request) {
		EntityCriteria<Review> criteria = EntityCriteria.of(Review.class);
		criteria.createCriteria("update").add(Restrictions.eq("request", request));
		criteria.addOrder(Order.asc("date"));
		return findAll(criteria);
	}

	@Transactional
	@Override
	public void delete(User user, PullRequest request) {
		for (Iterator<Review> it = request.getReviews().iterator(); it.hasNext();) {
			Review review = it.next();
			if (review.getUser().equals(user)) {
				delete(review);
				it.remove();
			}
		}
		
		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(new Date());
		statusChange.setRequest(request);
		statusChange.setType(Type.WITHDRAWED_REVIEW);
		statusChange.setNote("Review of user '" + user.getDisplayName() + "' is withdrawed");
		statusChange.setUser(userManager.getCurrent());
		pullRequestStatusChangeManager.save(statusChange);
		
		request.setLastEvent(statusChange);
		pullRequestManager.save(request);
	}

	@Override
	public boolean canModify(User user, Project project, String branch, String file) {
		BranchProtection branchProtection = project.getBranchProtection(branch);
		if (branchProtection != null) {
			if (branchProtection.getReviewAppointment(project) != null 
					&& !branchProtection.getReviewAppointment(project).matches(user)) {
				return false;
			}
			FileProtection fileProtection = branchProtection.getFileProtection(file);
			if (fileProtection != null && !fileProtection.getReviewAppointment(project).matches(user))
				return false;
		}			
		return true;
	}

	private Set<String> getChangedFiles(Project project, ObjectId oldObjectId, ObjectId newObjectId) {
		Set<String> changedFiles = new HashSet<>();
		try (TreeWalk treeWalk = new TreeWalk(project.getRepository())) {
			treeWalk.setFilter(TreeFilter.ANY_DIFF);
			treeWalk.setRecursive(true);
			RevCommit oldCommit = project.getRevCommit(oldObjectId);
			RevCommit newCommit = project.getRevCommit(newObjectId);
			treeWalk.addTree(oldCommit.getTree());
			treeWalk.addTree(newCommit.getTree());
			while (treeWalk.next()) {
				changedFiles.add(treeWalk.getPathString());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return changedFiles;
	}
	
	@Override
	public boolean canPush(User user, Project project, String branch, ObjectId oldObjectId, ObjectId newObjectId) {
		BranchProtection branchProtection = project.getBranchProtection(branch);
		if (branchProtection != null) {
			if (branchProtection.getReviewAppointment(project) != null 
					&& !branchProtection.getReviewAppointment(project).matches(user)) {
				return false;
			}
			
			for (String changedFile: getChangedFiles(project, oldObjectId, newObjectId)) {
				FileProtection fileProtection = branchProtection.getFileProtection(changedFile);
				if (fileProtection != null && !fileProtection.getReviewAppointment(project).matches(user))
					return false;
			}
		}
		return true;
	}

	private static class ReviewStatusImpl implements ReviewStatus {

		private final PullRequest request;
		
		private final List<User> awaitingReviewers = new ArrayList<>();
		
		private final Map<User, Review> effectiveReviews = new HashMap<>();
		
		@Override
		public List<User> getAwaitingReviewers() {
			return awaitingReviewers;
		}

		@Override
		public Map<User, Review> getEffectiveReviews() {
			return effectiveReviews;
		}

		@Nullable
		private ReviewInvitation getInvitation(User user) {
			for (ReviewInvitation invitation: request.getReviewInvitations()) {
				if (invitation.getUser().equals(user))
					return invitation;
			}
			return null;
		}
		
		public ReviewStatusImpl(PullRequest request) {
			this.request = request;
			
			for (ReviewInvitation invitation: request.getReviewInvitations()) {
				if (!invitation.getUser().equals(request.getSubmitter()) 
						&& invitation.getType() == ReviewInvitation.Type.MANUAL) {
					Review effectiveReview = invitation.getUser().getReviewAfter(request.getLatestUpdate());
					if (effectiveReview == null) 
						awaitingReviewers.add(invitation.getUser());
					else
						effectiveReviews.put(effectiveReview.getUser(), effectiveReview);
				}
			}

			BranchProtection branchProtection = request.getTargetProject().getBranchProtection(request.getTargetBranch());
			if (branchProtection != null) {
				ReviewAppointment appointment = branchProtection.getReviewAppointment(request.getTargetProject());
				if (appointment != null) 
					checkReviews(appointment, request.getLatestUpdate());

				Set<FileProtection> checkedFileProtections = new HashSet<>();
				for (int i=request.getSortedUpdates().size()-1; i>=0; i--) {
					if (checkedFileProtections.containsAll(branchProtection.getFileProtections()))
						break;
					PullRequestUpdate update = request.getSortedUpdates().get(i);
					for (String file: update.getChangedFiles()) {
						FileProtection fileProtection = branchProtection.getFileProtection(file);
						if (fileProtection != null && !checkedFileProtections.contains(fileProtection)) {
							checkedFileProtections.add(fileProtection);
							checkReviews(fileProtection.getReviewAppointment(request.getTargetProject()), update);
						}
					}
				}
			}
			
			if (request.getSubmitter() == null || !request.getSubmitter().asSubject().isPermitted(
					new ProjectPermission(request.getTargetProject(), ProjectPrivilege.WRITE))) {
				Collection<User> writers = SecurityUtils.findUsersCan(request.getTargetProject(), 
						ProjectPrivilege.WRITE);
				checkReviews(writers, 1, request.getLatestUpdate(), true);
			}
			
			for (Iterator<Map.Entry<User, Review>> it = effectiveReviews.entrySet().iterator(); it.hasNext();) {
				Review review = it.next().getValue();
				if (review.isCheckMerged()) {
					if (request.getMergePreview() == null 
							|| !review.getCommit().equals(request.getMergePreview().getMerged())) {
						if (!awaitingReviewers.contains(review.getUser()))
							awaitingReviewers.add(review.getUser());
						it.remove();
					}
				}
			}
			
		}
		
		private void checkReviews(ReviewAppointment appointment, PullRequestUpdate update) {
			for (User user: appointment.getUsers()) {
				if (!user.equals(request.getSubmitter()) && !awaitingReviewers.contains(user)) {
					Review effectiveReview = user.getReviewAfter(update);
					if (effectiveReview == null) {
						awaitingReviewers.add(user);
						inviteReviewer(user);
					} else {
						effectiveReviews.put(effectiveReview.getUser(), effectiveReview);
					}
				}
			}
			
			for (Map.Entry<Group, Integer> entry: appointment.getGroups().entrySet()) {
				Group group = entry.getKey();
				int requiredCount = entry.getValue();
				checkReviews(group.getMembers(), requiredCount, update, false);
			}
		}
		
		private void checkReviews(Collection<User> users, int requiredCount, PullRequestUpdate update, 
				boolean excludeAutoReviews) {
			if (requiredCount == 0)
				requiredCount = users.size();
			
			int effectiveCount = 0;
			Set<User> potentialReviewers = new HashSet<>();
			for (User user: users) {
				if (user.equals(request.getSubmitter())) {
					effectiveCount++;
				} else if (awaitingReviewers.contains(user)) {
					requiredCount--;
				} else {
					Review effectiveReview = user.getReviewAfter(update);
					if (effectiveReview != null) {
						if (!excludeAutoReviews || !effectiveReview.isAutoCheck()) {						
							effectiveCount++;
						} else {
							logger.warn("Auto review made by {} is ignored while checking reviews of pull request #{}", 
									user.getDisplayName(), update.getRequest().getNumber());
						}
					} else {
						potentialReviewers.add(user);
					}
				}
				if (effectiveCount >= requiredCount)
					break;
			}

			int missingCount = requiredCount - effectiveCount;
			Set<User> reviewers = new HashSet<>();
			
			List<User> candidateReviewers = new ArrayList<>();
			for (User user: potentialReviewers) {
				ReviewInvitation invitation = getInvitation(user);
				if (invitation != null && invitation.getType() != ReviewInvitation.Type.EXCLUDE)
					candidateReviewers.add(user);
			}
			
			sortByContributions(candidateReviewers, update);
			for (User user: candidateReviewers) {
				reviewers.add(user);
				if (reviewers.size() == missingCount)
					break;
			}
			
			if (reviewers.size() < missingCount) {
				candidateReviewers = new ArrayList<>();
				for (User user: potentialReviewers) {
					ReviewInvitation invitation = getInvitation(user);
					if (invitation == null) 
						candidateReviewers.add(user);
				}
				sortByContributions(candidateReviewers, update);
				for (User user: candidateReviewers) {
					reviewers.add(user);
					if (reviewers.size() == missingCount)
						break;
				}
			}
			if (reviewers.size() < missingCount) {
				List<ReviewInvitation> excludedInvitations = new ArrayList<>();
				for (User user: potentialReviewers) {
					ReviewInvitation invitation = getInvitation(user);
					if (invitation != null && invitation.getType() == ReviewInvitation.Type.EXCLUDE)
						excludedInvitations.add(invitation);
				}
				excludedInvitations.sort(Comparator.comparing(ReviewInvitation::getDate));
				
				for (ReviewInvitation invitation: excludedInvitations) {
					reviewers.add(invitation.getUser());
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

			for (User user: reviewers) {
				awaitingReviewers.add(user);
				inviteReviewer(user);
			}
		}
		
		private void sortByContributions(List<User> users, PullRequestUpdate update) {
			CommitInfoManager commitInfoManager = GitPlex.getInstance(CommitInfoManager.class);
			Map<User, Integer> contributions = new HashMap<>();
			for (User user: users)
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
		
		private int addContributions(Map<User, Integer> contributions, CommitInfoManager commitInfoManager, 
				String file, PullRequestUpdate update) {
			int addedContributions = 0;
			for (Map.Entry<User, Integer> entry: contributions.entrySet()) {
				User user = entry.getKey();
				int fileContribution = commitInfoManager.getContributions(update.getRequest().getTargetProject(), user, 
						file);
				entry.setValue(entry.getValue() + fileContribution);
				addedContributions += fileContribution;
			}
			return addedContributions;
		}
		
		private void inviteReviewer(User user) {
			ReviewInvitation invitation = getInvitation(user);
			if (invitation != null) {
				invitation.setType(ReviewInvitation.Type.RULE);
			} else {
				invitation = new ReviewInvitation();
				invitation.setRequest(request);
				invitation.setUser(user);
				invitation.setType(ReviewInvitation.Type.RULE);
				request.getReviewInvitations().add(invitation);
			}
		}
		
	}	
}
