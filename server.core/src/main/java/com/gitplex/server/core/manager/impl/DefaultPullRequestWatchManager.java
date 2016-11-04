package com.gitplex.server.core.manager.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Sets;
import com.gitplex.commons.git.NameAndEmail;
import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.loader.Listen;
import com.gitplex.commons.markdown.MarkdownManager;
import com.gitplex.commons.wicket.editable.EditableUtils;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.BranchWatch;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.entity.PullRequestReviewInvitation;
import com.gitplex.server.core.entity.PullRequestUpdate;
import com.gitplex.server.core.entity.PullRequestWatch;
import com.gitplex.server.core.entity.PullRequestStatusChange.Type;
import com.gitplex.server.core.event.MarkdownAware;
import com.gitplex.server.core.event.pullrequest.IntegrationPreviewCalculated;
import com.gitplex.server.core.event.pullrequest.PullRequestChangeEvent;
import com.gitplex.server.core.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.server.core.event.pullrequest.PullRequestCodeCommentCreated;
import com.gitplex.server.core.event.pullrequest.PullRequestCommentCreated;
import com.gitplex.server.core.event.pullrequest.PullRequestOpened;
import com.gitplex.server.core.event.pullrequest.PullRequestReviewInvitationChanged;
import com.gitplex.server.core.event.pullrequest.PullRequestStatusChangeEvent;
import com.gitplex.server.core.event.pullrequest.PullRequestUpdated;
import com.gitplex.server.core.event.pullrequest.PullRequestVerificationRunning;
import com.gitplex.server.core.manager.AccountManager;
import com.gitplex.server.core.manager.BranchWatchManager;
import com.gitplex.server.core.manager.MailManager;
import com.gitplex.server.core.manager.PullRequestWatchManager;
import com.gitplex.server.core.manager.UrlManager;
import com.gitplex.server.core.manager.VisitInfoManager;
import com.gitplex.server.core.util.markdown.MentionParser;

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

				String url;
				if (event instanceof PullRequestCommentCreated)
					url = urlManager.urlFor(((PullRequestCommentCreated)event).getComment());
				else if (event instanceof PullRequestStatusChangeEvent) 
					url = urlManager.urlFor(((PullRequestStatusChangeEvent)event).getStatusChange());
				else if (event instanceof PullRequestCodeCommentCreated)
					url = urlManager.urlFor(((PullRequestCodeCommentCreated)event).getComment(), event.getRequest());
				else if (event instanceof PullRequestCodeCommentActivityEvent)
					url = urlManager.urlFor(((PullRequestCodeCommentActivityEvent)event).getActivity(), event.getRequest());
				else 
					url = urlManager.urlFor(event.getRequest());
				
				String subject = String.format("You are mentioned in pull request #%d (%s)", 
						request.getNumber(), request.getTitle());
				String body = String.format("%s."
						+ "<p style='margin: 16px 0; padding-left: 16px; border-left: 4px solid #CCC;'>%s"
						+ "<p style='margin: 16px 0;'>"
						+ "For details, please visit <a href='%s'>%s</a>", 
						subject, markdownManager.escape(markdown), url, url);
				
				mailManager.sendMailAsync(mentionUsers.stream().map(Account::getEmail).collect(Collectors.toList()), 
						subject, decorateBody(body));
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
			mailManager.sendMailAsync(Sets.newHashSet(request.getAssignee().getEmail()), subject, decorateBody(body));
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
			mailManager.sendMailAsync(watchUsers.stream().map(Account::getEmail).collect(Collectors.toList()), subject, body);
		}
		
	}
	
	private String decorateBody(String body) {
		return String.format(""
				+ "%s"
				+ "<p style='margin: 16px 0;'>"
				+ "-- Sent by GitPlex", 
				body);
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
