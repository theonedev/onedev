package com.pmease.gitplex.core.manager.impl;

import static com.pmease.gitplex.core.model.PullRequest.Event.COMMENTED;
import static com.pmease.gitplex.core.model.PullRequest.Event.DISCARDED;
import static com.pmease.gitplex.core.model.PullRequest.Event.INTEGRATED;
import static com.pmease.gitplex.core.model.PullRequest.Event.OPENED;
import static com.pmease.gitplex.core.model.PullRequest.Event.REOPENED;
import static com.pmease.gitplex.core.model.PullRequest.Event.REVIEWED;
import static com.pmease.gitplex.core.model.PullRequest.Event.UPDATED;
import static com.pmease.gitplex.core.model.PullRequest.Event.VERIFIED;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Sets;
import com.pmease.commons.git.Commit;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.manager.PullRequestWatchManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.BranchWatch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.Event;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestCommentReply;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.PullRequestVisit;
import com.pmease.gitplex.core.model.PullRequestWatch;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.ReviewInvitation;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultPullRequestWatchManager implements PullRequestWatchManager {

	private final Dao dao;
	
	private final UserManager userManager;
	
	private final MailManager mailManager;
	
	@Inject
	public DefaultPullRequestWatchManager(Dao dao, UserManager userManager, MailManager mailManager) {
		this.dao = dao;
		this.userManager = userManager;
		this.mailManager = mailManager;
	}
	
	@Transactional
	@Override
	public void onOpened(PullRequest request) {
		for (BranchWatch branchWatch: request.getTarget().getWatches()) {
			watch(request, branchWatch.getUser(), 
					"You are set to watch this pull request as you are watching the target branch.");
		}
		
		watch(request, request.getSubmitter(), "You are set to watch this pull request as you've opened it.");		

		addParticipantsToWatchList(request.getLatestUpdate());
		
		Collection<User> excludingUsers = new HashSet<>();
		excludingUsers.add(request.getSubmitter());
		if (request.getAssignee() != null)
			excludingUsers.add(request.getAssignee());
		
		notify(request, excludingUsers, OPENED);
	}

	@Transactional
	private void addParticipantsToWatchList(PullRequestUpdate update) {
		if (update.getUser() != null) {
			watch(update.getRequest(), update.getUser(), 
					"You are set to watch this pull request as you updated it.");
		}
		Set<String> emails = new HashSet<>();
		for (Commit commit: update.getCommits()) {
			emails.add(commit.getCommitter().getEmailAddress());
			emails.add(commit.getAuthor().getEmailAddress());
		}
		for (String email: emails) {
			User user = userManager.findByEmail(email);
			if (user != null) 
				watch(update.getRequest(), user, "You are set to watch this pull request as it contains your commits.");
		}
	}

	@Transactional
	@Override
	public void onReopened(PullRequest request, User user, String comment) {
		watch(request, user, "You are set to watch this pull request as you've reopened it.");
		
		Collection<User> excludingUsers = new HashSet<>();
		excludingUsers.add(user);
		if (request.getAssignee() != null)
			excludingUsers.add(request.getAssignee());
		notify(request, excludingUsers, REOPENED);
	}

	@Transactional
	@Override
	public void onUpdated(PullRequestUpdate update) {
		addParticipantsToWatchList(update);

		if (update.getUser() != null)
			notify(update.getRequest(), Sets.newHashSet(update.getUser()), UPDATED);
		else
			notify(update.getRequest(), new HashSet<User>(), UPDATED);
	}

	@Transactional
	@Override
	public void onMentioned(PullRequest request, User user, String content) {
		watch(request, user, "You are set to watch this pull request as you are mentioned in comment.");
	}

	@Transactional
	@Override
	public void onCommented(PullRequestComment comment) {
		watch(comment.getRequest(), comment.getUser(), 
				"You are set to watch this pull request as you've commented.");
		notify(comment.getRequest(), Sets.newHashSet(comment.getUser()), COMMENTED);
	}

	@Transactional
	@Override
	public void onCommentReplied(PullRequestCommentReply reply) {
		watch(reply.getComment().getRequest(), reply.getUser(), 
				"You are set to watch this pull request as you've commented.");
		notify(reply.getComment().getRequest(), Sets.newHashSet(reply.getUser()), COMMENTED);
	}

	@Transactional
	@Override
	public void onReviewed(Review review, String comment) {
		notify(review.getUpdate().getRequest(), Sets.newHashSet(review.getReviewer()), REVIEWED);
	}

	@Transactional
	@Override
	public void onAssigned(PullRequest request) {
		watch(request, request.getAssignee(), 
				"You are set to watch this pull request as you've assigned to integrate it.");
	}

	@Transactional
	@Override
	public void onVerified(PullRequest request) {
		notify(request, new HashSet<User>(), VERIFIED);
	}

	@Transactional
	@Override
	public void onIntegrated(PullRequest request, User user, String comment) {
		if (user != null)
			notify(request, Sets.newHashSet(user), INTEGRATED);
		else
			notify(request, new HashSet<User>(), INTEGRATED);
	}

	@Transactional
	@Override
	public void onDiscarded(PullRequest request, User user, String comment) {
		if (user != null)
			notify(request, Sets.newHashSet(user), DISCARDED);
		else
			notify(request, new HashSet<User>(), DISCARDED);
	}

	@Override
	public void onIntegrationPreviewCalculated(PullRequest request) {
	}

	@Transactional
	@Override
	public void onInvitingReview(ReviewInvitation invitation) {
		watch(invitation.getRequest(), invitation.getReviewer(), 
				"You are set to watch this pull request as you are invited to review it.");
	}

	@Override
	public void pendingIntegration(PullRequest request) {
	}

	@Override
	public void pendingUpdate(PullRequest request) {
	}

	@Override
	public void pendingApproval(PullRequest request) {
	}

	@Transactional
	private void watch(PullRequest request, User user, String reason) {
		PullRequestWatch watch = request.getWatch(user);
		if (watch == null) {
			watch = new PullRequestWatch();
			watch.setRequest(request);
			watch.setUser(user);
			watch.setReason(reason);
			request.getWatches().add(watch);
			dao.persist(watch);
		}
	}

	@Transactional
	private void notify(PullRequest request, Collection<User> excludingUsers, Event event) {
		Map<User, Date> visitDates = new HashMap<>();
		for (PullRequestVisit visit: request.getVisits()) 
			visitDates.put(visit.getUser(), visit.getDate());
		
		Collection<User> usersToNotify = new HashSet<>();
		
		for (PullRequestWatch watch: request.getWatches()) {
			if (!watch.isIgnore() && !excludingUsers.contains(watch.getUser())) { 
				if (event == OPENED || event != request.getLastEvent()) {
					usersToNotify.add(watch.getUser());
				} else {
					Date visitDate = visitDates.get(watch.getUser());
					if (visitDate != null && visitDate.after(request.getLastEventDate()))
						usersToNotify.add(watch.getUser());
				}
			}
		}
		
		if (!usersToNotify.isEmpty()) {
			mailManager.sendMail(usersToNotify, 
					event + ": Pull request #" + request.getId() + " (" + request.getTitle() + ")", 
					"Visit url for details");
		}
		
		request.setLastEventDate(new Date());
		request.setLastEvent(event);
		
		dao.persist(request);
	}
	
}
