package com.pmease.gitplex.core.manager.impl;

import static com.pmease.gitplex.core.entity.PullRequest.Event.COMMENTED;
import static com.pmease.gitplex.core.entity.PullRequest.Event.DISCARDED;
import static com.pmease.gitplex.core.entity.PullRequest.Event.INTEGRATED;
import static com.pmease.gitplex.core.entity.PullRequest.Event.OPENED;
import static com.pmease.gitplex.core.entity.PullRequest.Event.REOPENED;
import static com.pmease.gitplex.core.entity.PullRequest.Event.REVIEWED;
import static com.pmease.gitplex.core.entity.PullRequest.Event.UPDATED;
import static com.pmease.gitplex.core.entity.PullRequest.Event.VERIFIED;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Sets;
import com.pmease.commons.git.NameAndEmail;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.BranchWatch;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequest.Event;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.PullRequestVisit;
import com.pmease.gitplex.core.entity.PullRequestWatch;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.BranchWatchManager;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestWatchManager;
import com.pmease.gitplex.core.manager.UrlManager;

@Singleton
public class DefaultPullRequestWatchManager extends AbstractEntityManager<PullRequestWatch> implements PullRequestWatchManager {

	private final AccountManager userManager;
	
	private final BranchWatchManager branchWatchManager;
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;

	private final PullRequestManager pullRequestManager;
	
	@Inject
	public DefaultPullRequestWatchManager(Dao dao, PullRequestManager pullRequestManager,
			AccountManager userManager, BranchWatchManager branchWatchManager, 
			MailManager mailManager, UrlManager urlManager) {
		super(dao);
		
		this.pullRequestManager = pullRequestManager;
		this.userManager = userManager;
		this.branchWatchManager = branchWatchManager;
		this.mailManager = mailManager;
		this.urlManager = urlManager;
	}
	
	@Transactional
	@Override
	public void onOpenRequest(PullRequest request) {
		for (BranchWatch branchWatch: branchWatchManager.findBy(request.getTargetDepot(), request.getTargetBranch())) {
			watch(request, branchWatch.getUser(), 
					"You are set to watch this pull request as you are watching the target branch.");
		}

		if (request.getSubmitter() != null)
			watch(request, request.getSubmitter(), "You are set to watch this pull request as you've opened it.");		

		addParticipantsToWatchList(request.getLatestUpdate());
		
		Collection<Account> excludingUsers = new HashSet<>();
		if (request.getSubmitter() != null)
			excludingUsers.add(request.getSubmitter());
		if (request.getAssignee() != null)
			excludingUsers.add(request.getAssignee());
		
		notify(request, excludingUsers, OPENED);
	}

	@Transactional
	private void addParticipantsToWatchList(PullRequestUpdate update) {
		// we use a name-email pair to filter off duplicate person as equals method of PersonIdent also takes 
		// "when" field into account
		Set<NameAndEmail> setOfNameAndEmail = new HashSet<>();
		for (RevCommit commit: update.getCommits()) {
			setOfNameAndEmail.add(new NameAndEmail(commit.getCommitterIdent().getName(), commit.getCommitterIdent().getEmailAddress()));
			setOfNameAndEmail.add(new NameAndEmail(commit.getAuthorIdent().getName(), commit.getAuthorIdent().getEmailAddress()));
		}
		for (NameAndEmail nameAndEmail: setOfNameAndEmail) {
			PersonIdent person = new PersonIdent(nameAndEmail.getName(), nameAndEmail.getEmailAddress());
			Account user = userManager.findByPerson(person);
			if (user != null) 
				watch(update.getRequest(), user, "You are set to watch this pull request as it contains your commits.");
		}
	}

	@Transactional
	@Override
	public void onReopenRequest(PullRequest request, Account user, String comment) {
		watch(request, user, "You are set to watch this pull request as you've reopened it.");
		
		Collection<Account> excludingUsers = new HashSet<>();
		excludingUsers.add(user);
		if (request.getAssignee() != null)
			excludingUsers.add(request.getAssignee());
		notify(request, excludingUsers, REOPENED);
	}

	@Transactional
	@Override
	public void onUpdateRequest(PullRequestUpdate update) {
		addParticipantsToWatchList(update);

		notify(update.getRequest(), new HashSet<Account>(), UPDATED);
	}

	@Transactional
	@Override
	public void onCommentRequest(PullRequestComment comment) {
		watch(comment.getRequest(), comment.getUser(), 
				"You are set to watch this pull request as you've commented.");
		notify(comment.getRequest(), Sets.newHashSet(comment.getUser()), COMMENTED);
	}

	@Transactional
	@Override
	public void onReviewRequest(Review review, String comment) {
		notify(review.getUpdate().getRequest(), Sets.newHashSet(review.getReviewer()), REVIEWED);
	}

	@Transactional
	@Override
	public void onAssignRequest(PullRequest request) {
		watch(request, request.getAssignee(), 
				"You are set to watch this pull request as you've assigned to integrate it.");
	}

	@Transactional
	@Override
	public void onVerifyRequest(PullRequest request) {
		notify(request, new HashSet<Account>(), VERIFIED);
	}

	@Transactional
	@Override
	public void onIntegrateRequest(PullRequest request, Account user, String comment) {
		if (user != null)
			notify(request, Sets.newHashSet(user), INTEGRATED);
		else
			notify(request, new HashSet<Account>(), INTEGRATED);
	}

	@Transactional
	@Override
	public void onDiscardRequest(PullRequest request, Account user, String comment) {
		if (user != null)
			notify(request, Sets.newHashSet(user), DISCARDED);
		else
			notify(request, new HashSet<Account>(), DISCARDED);
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
	private void watch(PullRequest request, Account user, String reason) {
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
	private void notify(PullRequest request, Collection<Account> excludingUsers, Event event) {
		Map<Account, Date> visitDates = new HashMap<>();
		for (PullRequestVisit visit: request.getVisits()) 
			visitDates.put(visit.getUser(), visit.getDate());
		
		Collection<Account> usersToNotify = new HashSet<>();
		
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
			String subject = String.format("%s pull request #%d (%s)", 
					event.toString(), request.getId(), request.getTitle()); 
			String url = urlManager.urlFor(request);
			String body = String.format("Dear Users,"
					+ "<p style='margin: 16px 0;'>%s pull request #%d (%s). "
					+ "<p style='margin: 16px 0;'>Visit <a href='%s'>%s</a> for details."
					+ "<p style='margin: 16px 0;'>-- Sent by GitPlex"
					+ "<p style='margin: 16px 0; font-size: 12px; color: #888;'>"
					+ "You receive this email as you are watching the branch or pull request.",
					event.toString(), request.getId(), request.getTitle(), url, url);
			mailManager.sendMail(usersToNotify, subject, body);
		}
		
		request.setLastEventDate(new Date());
		request.setLastEvent(event);
		
		pullRequestManager.save(request);
	}

	@Transactional
	@Override
	public void onMentionAccount(PullRequest request, Account user) {
		watch(request, user, "You are set to watch this pull request as you are mentioned.");
	}

	@Override
	public void onMentionAccount(PullRequestComment comment, Account user) {
		watch(comment.getRequest(), user, "You are set to watch this pull request as you are mentioned.");
	}

	@Override
	public void onDeleteRequest(PullRequest request) {
	}

}
