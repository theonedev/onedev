package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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
import com.pmease.commons.loader.Listen;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.commons.wicket.editable.EditableUtils;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.BranchWatch;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestReviewInvitation;
import com.pmease.gitplex.core.entity.PullRequestStatusChange.Type;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.PullRequestWatch;
import com.pmease.gitplex.core.event.MarkdownAware;
import com.pmease.gitplex.core.event.pullrequest.IntegrationPreviewCalculated;
import com.pmease.gitplex.core.event.pullrequest.PullRequestChangeEvent;
import com.pmease.gitplex.core.event.pullrequest.PullRequestOpened;
import com.pmease.gitplex.core.event.pullrequest.PullRequestReviewInvitationChanged;
import com.pmease.gitplex.core.event.pullrequest.PullRequestStatusChangeEvent;
import com.pmease.gitplex.core.event.pullrequest.PullRequestUpdated;
import com.pmease.gitplex.core.event.pullrequest.PullRequestVerificationRunning;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.BranchWatchManager;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.manager.PullRequestWatchManager;
import com.pmease.gitplex.core.manager.UrlManager;
import com.pmease.gitplex.core.manager.VisitInfoManager;
import com.pmease.gitplex.core.util.markdown.MentionParser;

@Singleton
public class DefaultPullRequestWatchManager extends AbstractEntityManager<PullRequestWatch> 
		implements PullRequestWatchManager {

	private final AccountManager userManager;
	
	private final BranchWatchManager branchWatchManager;
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final VisitInfoManager visitInfoManager;
	
	private final MarkdownManager markdownManager;
	
	@Inject
	public DefaultPullRequestWatchManager(Dao dao, AccountManager userManager, MarkdownManager markdownManager, 
			BranchWatchManager branchWatchManager, MailManager mailManager, UrlManager urlManager, 
			VisitInfoManager visitInfoManager) {
		super(dao);
		this.userManager = userManager;
		this.branchWatchManager = branchWatchManager;
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.visitInfoManager = visitInfoManager;
		this.markdownManager = markdownManager;
	}
	
	@Transactional
	@Listen
	public void on(PullRequestChangeEvent event) {
		if (event instanceof IntegrationPreviewCalculated || event instanceof PullRequestVerificationRunning) {
			return;
		}
		String eventType;
		Type statusChangeType = null;
		if (event instanceof PullRequestStatusChangeEvent) {
			statusChangeType = ((PullRequestStatusChangeEvent) event).getStatusChange().getType();
			eventType = statusChangeType.getName();
			if (statusChangeType == Type.SOURCE_BRANCH_DELETED || statusChangeType == Type.SOURCE_BRANCH_RESTORED) {
				return;
			}
		} else {
			eventType = EditableUtils.getName(event.getClass());
		}
		
		PullRequest request = event.getRequest();
		Account user = event.getUser();
		
		/*
		 * verification is often performed automatically by robots (such as CI system), and it does not make
		 * sense to make them watching the pull request
		 */
		if (user != null && statusChangeType != Type.VERIFICATION_SUCCEEDED && statusChangeType != Type.VERIFICATION_FAILED) {
			watch(request, user, 
					"You've set to watch this pull request as you've " + eventType);
		}
		if (event instanceof PullRequestOpened) {
			if (request.getAssignee() != null)
				watch(request, request.getAssignee(), "You are set to watch this pull request as you are assigned to integrate it.");
			for (BranchWatch branchWatch: branchWatchManager.find(request.getTargetDepot(), request.getTargetBranch())) {
				watch(request, branchWatch.getUser(), 
						"You are set to watch this pull request as you are watching the target branch.");
			}
			makeContributorsWatching(request.getLatestUpdate());
		} else if (statusChangeType == Type.ASSIGNED) {
			watch(request, request.getAssignee(), "You are set to watch this pull request as you are assigned to integrate it.");
		} else if (event instanceof PullRequestUpdated) {
			PullRequestUpdated updated = (PullRequestUpdated) event;
			makeContributorsWatching(updated.getUpdate());
		}

		Collection<Account> mentionUsers = new HashSet<>();
		if (event instanceof MarkdownAware) {
			MarkdownAware markdownAware = (MarkdownAware) event;
			String markdown = markdownAware.getMarkdown();
			if (markdown != null) {
				String html = markdownManager.parse(markdown);
				mentionUsers.addAll(new MentionParser().parseMentions(html));
				for (Account mention: mentionUsers) {
					watch(request, mention, "You are set to watch this pull request as you are mentioned in code comment.");
				}
			}
		} 		
		Collection<Account> watchUsers = new HashSet<>();
		
		for (PullRequestWatch watch: request.getWatches()) {
			if (!watch.isIgnore() && !watch.getUser().equals(event.getUser())) { 
				if (request.getLastEvent() == null) {
					watchUsers.add(watch.getUser());
				} else {
					Date visitDate = visitInfoManager.getVisitDate(watch.getUser(), request);
					if (visitDate == null || visitDate.getTime()<request.getLastEvent().getDate().getTime()) {
						if (!request.getLastEvent().getType().equals(eventType)) { 
							watchUsers.add(watch.getUser());
						} 
					} else {
						watchUsers.add(watch.getUser());
					}
				}
			}
		}

		// mentioned users will be receiving email separately, so we do not need to notify
		// them here
		watchUsers.removeAll(mentionUsers);

		if ((event instanceof PullRequestOpened || statusChangeType == Type.ASSIGNED) 
				&& request.getAssignee() != null
				&& !request.getAssignee().equals(event.getUser())) {
			watchUsers.remove(request.getAssignee());
			String subject = String.format("You are assigned with pull request #%d (%s)", 
					request.getNumber(), request.getTitle());
			String url = urlManager.urlFor(request);
			String body = String.format("You are assigned with pull request #%d (%s).<br>"
					+ "Please visit <a href='%s'>%s</a> for details.",
					request.getNumber(), request.getTitle(), url, url);
			mailManager.sendMailAsync(Sets.newHashSet(request.getAssignee()), subject, decorate(user, body));
		}
		
		if (!watchUsers.isEmpty()) {
			String subject, body;
			String url = urlManager.urlFor(request);
			if (event instanceof PullRequestOpened) {
				subject = String.format("Newly opened pull request - #%d (%s)", 
						request.getNumber(), request.getTitle()); 
				body = String.format("Dear Users,"
						+ "<p style='margin: 16px 0;'>A new pull request is opened - #%d (%s)"
						+ "<p style='margin: 16px 0;'>Visit <a href='%s'>%s</a> for details."
						+ "<p style='margin: 16px 0;'>-- Sent by GitPlex"
						+ "<p style='margin: 16px 0; font-size: 12px; color: #888;'>"
						+ "You receive this email as you are watching target branch.",
						request.getNumber(), request.getTitle(), url, url);
			} else {
				subject = String.format("New activity in pull request #%d (%s) - %s", 
						request.getNumber(), request.getTitle(), eventType); 
				body = String.format("Dear Users,"
						+ "<p style='margin: 16px 0;'>There is new activity in pull request #%d (%s) - %s "
						+ "<p style='margin: 16px 0;'>Visit <a href='%s'>%s</a> for details."
						+ "<p style='margin: 16px 0;'>-- Sent by GitPlex"
						+ "<p style='margin: 16px 0; font-size: 12px; color: #888;'>"
						+ "You receive this email as you are watching the pull request.",
						request.getNumber(), request.getTitle(), eventType, url, url);
			}
			mailManager.sendMailAsync(watchUsers, subject, body);
		}
		
	}
	
	private String decorate(Account user, String body) {
		return String.format("Dear %s, "
				+ "<p style='margin: 16px 0;'>"
				+ "%s"
				+ "<p style='margin: 16px 0;'>"
				+ "-- Sent by GitPlex", 
				user.getDisplayName(), body);
	}
	
	private void makeContributorsWatching(PullRequestUpdate update) {
		// we use a name-email pair to filter off duplicate person as equals method of PersonIdent also takes 
		// "when" field into account
		Set<NameAndEmail> nameAndEmails = new HashSet<>();
		for (RevCommit commit: update.getCommits()) {
			nameAndEmails.add(new NameAndEmail(commit.getCommitterIdent()));
			nameAndEmails.add(new NameAndEmail(commit.getAuthorIdent()));
		}
		for (NameAndEmail nameAndEmail: nameAndEmails) {
			PersonIdent person = new PersonIdent(nameAndEmail.getName(), nameAndEmail.getEmailAddress());
			Account user = userManager.find(person);
			if (user != null) 
				watch(update.getRequest(), user, "You are set to watch this pull request as it contains your commits.");
		}
	}

	@Transactional
	@Listen
	public void on(PullRequestReviewInvitationChanged event) {
		PullRequestReviewInvitation invitation = event.getInvitation();
		if (invitation.getStatus() != PullRequestReviewInvitation.Status.EXCLUDED) {
			watch(invitation.getRequest(), invitation.getUser(), 
					"You are set to watch this pull request as you are invited to review it.");
		}
	}
	
	private void watch(PullRequest request, Account user, String reason) {
		PullRequestWatch watch = request.getWatch(user);
		if (watch == null) {
			watch = new PullRequestWatch();
			watch.setRequest(request);
			watch.setUser(user);
			watch.setReason(reason);
			request.getWatches().add(watch);
			save(watch);
		}
	}

}
