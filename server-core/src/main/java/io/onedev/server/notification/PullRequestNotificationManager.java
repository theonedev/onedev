package io.onedev.server.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.Listen;
import io.onedev.server.entitymanager.PullRequestWatchManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.pullrequest.PullRequestAssigned;
import io.onedev.server.event.pullrequest.PullRequestBuildEvent;
import io.onedev.server.event.pullrequest.PullRequestChanged;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentCreated;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentReplied;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentStatusChanged;
import io.onedev.server.event.pullrequest.PullRequestCommented;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.event.pullrequest.PullRequestMergePreviewCalculated;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.event.pullrequest.PullRequestReviewRequested;
import io.onedev.server.event.pullrequest.PullRequestReviewerRemoved;
import io.onedev.server.event.pullrequest.PullRequestUnassigned;
import io.onedev.server.event.pullrequest.PullRequestUpdated;
import io.onedev.server.infomanager.UserInfoManager;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.QueryWatchBuilder;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;

@Singleton
public class PullRequestNotificationManager extends AbstractNotificationManager {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final PullRequestWatchManager pullRequestWatchManager;
	
	private final UserInfoManager userInfoManager;
	
	private final UserManager userManager;
	
	@Inject
	public PullRequestNotificationManager(MailManager mailManager, UrlManager urlManager, 
			MarkdownManager markdownManager, PullRequestWatchManager pullRequestWatchManager, 
			UserInfoManager userInfoManager, UserManager userManager, SettingManager settingManager) {
		super(markdownManager, settingManager);
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.pullRequestWatchManager = pullRequestWatchManager;
		this.userInfoManager = userInfoManager;
		this.userManager = userManager;
	}
	
	@Transactional
	@Listen
	public void on(PullRequestEvent event) {
		PullRequest request = event.getRequest();
		User user = event.getUser();

		String url;
		if (event instanceof PullRequestCommented)
			url = urlManager.urlFor(((PullRequestCommented)event).getComment());
		else if (event instanceof PullRequestChanged) 
			url = urlManager.urlFor(((PullRequestChanged)event).getChange());
		else if (event instanceof PullRequestCodeCommentCreated)
			url = urlManager.urlFor(((PullRequestCodeCommentCreated)event).getComment());
		else if (event instanceof PullRequestCodeCommentReplied)
			url = urlManager.urlFor(((PullRequestCodeCommentReplied)event).getReply());
		else if (event instanceof PullRequestCodeCommentStatusChanged)
			url = urlManager.urlFor(((PullRequestCodeCommentStatusChanged)event).getChange());
		else 
			url = urlManager.urlFor(request);
		
		for (Map.Entry<User, Boolean> entry: new QueryWatchBuilder<PullRequest>() {

			@Override
			protected PullRequest getEntity() {
				return request;
			}

			@Override
			protected Collection<? extends QueryPersonalization<?>> getQueryPersonalizations() {
				return request.getTargetProject().getPullRequestQueryPersonalizations();
			}

			@Override
			protected EntityQuery<PullRequest> parse(String queryString) {
				return PullRequestQuery.parse(request.getTargetProject(), queryString);
			}

			@Override
			protected Collection<? extends NamedQuery> getNamedQueries() {
				return request.getTargetProject().getNamedPullRequestQueries();
			}
			
		}.getWatches().entrySet()) {
			pullRequestWatchManager.watch(request, entry.getKey(), entry.getValue());
		}
		
		for (Map.Entry<User, Boolean> entry: new QueryWatchBuilder<PullRequest>() {

			@Override
			protected PullRequest getEntity() {
				return request;
			}

			@Override
			protected Collection<? extends QueryPersonalization<?>> getQueryPersonalizations() {
				return userManager.query().stream().map(it->it.getPullRequestQueryPersonalization()).collect(Collectors.toList());
			}

			@Override
			protected EntityQuery<PullRequest> parse(String queryString) {
				return PullRequestQuery.parse(null, queryString);
			}

			@Override
			protected Collection<? extends NamedQuery> getNamedQueries() {
				return settingManager.getPullRequestSetting().getNamedQueries();
			}
			
		}.getWatches().entrySet()) {
			pullRequestWatchManager.watch(request, entry.getKey(), entry.getValue());
		}
		
		Collection<User> notifiedUsers = Sets.newHashSet();
		if (user != null) {
			notifiedUsers.add(user); // no need to notify the user generating the event
			if (!user.isSystem())
				pullRequestWatchManager.watch(request, user, true);
		}
		
		User committer = null;
		if (event instanceof PullRequestUpdated) {
			PullRequestUpdated pullRequestUpdated = (PullRequestUpdated) event;
			Collection<User> committers = pullRequestUpdated.getCommitters();
			if (committers.size() == 1) {
				committer = committers.iterator().next();
				notifiedUsers.add(committer);
			}
			for (User each: committers) {
				if (!each.isSystem())
					pullRequestWatchManager.watch(request, each, true);
			}
		}
		
		String summary; 
		if (user != null)
			summary = user.getDisplayName() + " " + event.getActivity();
		else if (committer != null)
			summary = committer.getDisplayName() + " " + event.getActivity();
		else
			summary = StringUtils.capitalize(event.getActivity());
				
		String replyAddress = mailManager.getReplyAddress(request);
		boolean replyable = replyAddress != null;
		
		Set<User> reviewers = new HashSet<>();
		Set<User> assignees = new HashSet<>();
		if (event instanceof PullRequestOpened) {
			for (PullRequestReview review: request.getReviews()) {
				if (review.getStatus() == Status.PENDING)
					reviewers.add(review.getUser());
			}
			for (PullRequestAssignment assignment: request.getAssignments())
				assignees.add(assignment.getUser());
		} else if (event instanceof PullRequestChanged) {
			PullRequestChanged changeEvent = (PullRequestChanged) event;
			PullRequestChangeData changeData = changeEvent.getChange().getData();
			if ((changeData instanceof PullRequestApproveData
						|| changeData instanceof PullRequestRequestedForChangesData
						|| changeData instanceof PullRequestDiscardData)
					&& request.getSubmitter() != null && !notifiedUsers.contains(request.getSubmitter())) {
				String subject = String.format("[Pull Request %s] (%s) %s", request.getFQN(), 
						WordUtils.capitalize(changeData.getActivity()), request.getTitle());
				String threadingReferences = String.format("<%s-%s@onedev>", 
						changeData.getActivity().replace(' ', '-'), request.getUUID());
				EmailAddress emailAddress = request.getSubmitter().getPrimaryEmailAddress();
				if (emailAddress != null && emailAddress.isVerified()) {
					mailManager.sendMailAsync(Lists.newArrayList(emailAddress.getValue()), 
							Lists.newArrayList(), Lists.newArrayList(), subject, 
							getHtmlBody(event, summary, event.getHtmlBody(), url, replyable, null), 
							getTextBody(event, summary, event.getTextBody(), url, replyable, null), 
							replyAddress, threadingReferences);
				}
				notifiedUsers.add(request.getSubmitter());
			}
		} else if (event instanceof PullRequestAssigned) {
			assignees.add(((PullRequestAssigned) event).getAssignee());
		} else if (event instanceof PullRequestReviewRequested) {
			reviewers.add(((PullRequestReviewRequested) event).getReviewer());
		}

		for (User assignee: assignees) {
			pullRequestWatchManager.watch(request, assignee, true);
			if (!notifiedUsers.contains(assignee)) {
				String subject = String.format("[Pull Request %s] (Assigned) %s", 
						request.getFQN(), request.getTitle());
				String threadingReferences = String.format("<assigned-%s@onedev>", request.getUUID());
				String assignmentSummary;
				if (user != null)
					assignmentSummary = user.getDisplayName() + " assigned to you";
				else
					assignmentSummary = "Assigned to you";
				EmailAddress emailAddress = assignee.getPrimaryEmailAddress();
				if (emailAddress != null && emailAddress.isVerified()) {
					mailManager.sendMailAsync(Lists.newArrayList(emailAddress.getValue()), 
							Lists.newArrayList(), Lists.newArrayList(), subject, 
							getHtmlBody(event, assignmentSummary, event.getHtmlBody(), url, replyable, null), 
							getTextBody(event, assignmentSummary, event.getTextBody(), url, replyable, null), 
							replyAddress, threadingReferences);
				}				
				notifiedUsers.add(assignee);
			}
		}

		for (User reviewer: reviewers) {
			pullRequestWatchManager.watch(request, reviewer, true);
			if (!notifiedUsers.contains(reviewer)) {
				String subject = String.format("[Pull Request %s] (Review Request) %s", 
						request.getFQN(), request.getTitle());
				String threadingReferences = String.format("<review-invitation-%s@onedev>", request.getUUID());
				String reviewInvitationSummary;
				if (user != null)
					reviewInvitationSummary = user.getDisplayName() + " requested review from you";
				else
					reviewInvitationSummary = "Requested review from you";
				
				EmailAddress emailAddress = reviewer.getPrimaryEmailAddress();
				if (emailAddress != null && emailAddress.isVerified()) {
					mailManager.sendMailAsync(Lists.newArrayList(emailAddress.getValue()), 
							Lists.newArrayList(), Lists.newArrayList(), subject, 
							getHtmlBody(event, reviewInvitationSummary, event.getHtmlBody(), url, replyable, null), 
							getTextBody(event, reviewInvitationSummary, event.getTextBody(), url, replyable, null), 
							replyAddress, threadingReferences);
				}
				notifiedUsers.add(reviewer);
			}
		}
		
		Collection<String> notifiedEmailAddresses;
		if (event instanceof PullRequestCommented)
			notifiedEmailAddresses = ((PullRequestCommented) event).getNotifiedEmailAddresses();
		else
			notifiedEmailAddresses = new ArrayList<>();
		
		if (event.getRenderedMarkdown() != null) {
			for (String userName: new MentionParser().parseMentions(event.getRenderedMarkdown())) {
				User mentionedUser = userManager.findByName(userName);
				if (mentionedUser != null) { 
					pullRequestWatchManager.watch(request, mentionedUser, true);
					if (!isNotified(notifiedEmailAddresses, mentionedUser)) {
						String subject = String.format("[Pull Request %s] (Mentioned You) %s", request.getFQN(), request.getTitle());
						String threadingReferences = String.format("<mentioned-%s@onedev>", request.getUUID());
						
						EmailAddress emailAddress = mentionedUser.getPrimaryEmailAddress();
						if (emailAddress != null && emailAddress.isVerified()) {
							mailManager.sendMailAsync(Sets.newHashSet(emailAddress.getValue()), 
									Sets.newHashSet(), Sets.newHashSet(), subject, 
									getHtmlBody(event, summary, event.getHtmlBody(), url, replyable, null), 
									getTextBody(event, summary, event.getTextBody(), url, replyable, null),
									replyAddress, threadingReferences);
						}
						notifiedUsers.add(mentionedUser);
					}					
				}
			}
		} 
		
		boolean notifyWatchers = false;
		if (event instanceof PullRequestChanged) {
			PullRequestChangeData changeData = ((PullRequestChanged) event).getChange().getData();
			if (changeData instanceof PullRequestApproveData 
					|| changeData instanceof PullRequestRequestedForChangesData 
					|| changeData instanceof PullRequestMergeData 
					|| changeData instanceof PullRequestDiscardData
					|| changeData instanceof PullRequestReopenData) {
				notifyWatchers = true;
			}
		} else if (!(event instanceof PullRequestMergePreviewCalculated 
				|| event instanceof PullRequestBuildEvent
				|| event instanceof PullRequestReviewRequested
				|| event instanceof PullRequestReviewerRemoved
				|| event instanceof PullRequestAssigned
				|| event instanceof PullRequestUnassigned)) {
			notifyWatchers = true;
		}
		 
		if (notifyWatchers) {
			Collection<String> bccEmailAddresses = new HashSet<>();
			
			for (PullRequestWatch watch: request.getWatches()) {
				Date visitDate = userInfoManager.getPullRequestVisitDate(watch.getUser(), request);
				if (watch.isWatching() 
						&& (visitDate == null || visitDate.before(event.getDate())) 
						&& (!(event instanceof PullRequestUpdated) || !watch.getUser().equals(request.getSubmitter()))
						&& !notifiedUsers.contains(watch.getUser())
						&& !isNotified(notifiedEmailAddresses, watch.getUser())) {
					EmailAddress emailAddress = watch.getUser().getPrimaryEmailAddress();
					if (emailAddress != null && emailAddress.isVerified())
						bccEmailAddresses.add(emailAddress.getValue());
				}
			}

			if (!bccEmailAddresses.isEmpty()) {
				String subject = String.format("[Pull Request %s] (%s) %s", 
						request.getFQN(), (event instanceof PullRequestOpened)?"Opened":"Updated", request.getTitle());
				String threadingReferences = "<" + request.getUUID() + "@onedev>";
				Unsubscribable unsubscribable = new Unsubscribable(mailManager.getUnsubscribeAddress(request));
				String htmlBody = getHtmlBody(event, summary, event.getHtmlBody(), url, replyable, unsubscribable);
				String textBody = getTextBody(event, summary, event.getTextBody(), url, replyable, unsubscribable);
				mailManager.sendMailAsync(
						Lists.newArrayList(), Lists.newArrayList(),
						bccEmailAddresses, subject, htmlBody, textBody, 
						replyAddress, threadingReferences);
			}
		}				
	}
	
} 
