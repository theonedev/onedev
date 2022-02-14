package io.onedev.server.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.text.WordUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.Listen;
import io.onedev.server.entitymanager.PullRequestWatchManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.pullrequest.PullRequestBuildEvent;
import io.onedev.server.event.pullrequest.PullRequestChanged;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentCreated;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentReplied;
import io.onedev.server.event.pullrequest.PullRequestCommented;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.event.pullrequest.PullRequestMergePreviewCalculated;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.event.pullrequest.PullRequestUpdated;
import io.onedev.server.infomanager.UserInfoManager;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestReview;
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
import io.onedev.server.security.SecurityUtils;

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
				return request.getTargetProject().getPullRequestSetting().getNamedQueries(true);
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
			summary = event.getActivity();
		
		if (event instanceof PullRequestOpened) {
			for (PullRequestReview review: request.getReviews()) {
				if (review.getResult() == null) {
					// reviewers will be sent review invitation separately 
					notifiedUsers.add(review.getUser());
				}
			}
		}
		
		String replyAddress = mailManager.getReplyAddress(request);
		boolean replyable = replyAddress != null;
		if (event instanceof PullRequestChanged 
				&& request.getSubmitter() != null 
				&& !notifiedUsers.contains(request.getSubmitter())) {
			PullRequestChanged changeEvent = (PullRequestChanged) event;
			PullRequestChangeData changeData = changeEvent.getChange().getData();
			if (changeData instanceof PullRequestApproveData
					|| changeData instanceof PullRequestRequestedForChangesData
					|| changeData instanceof PullRequestDiscardData) { 
				String subject = String.format("[Pull Request %s] (%s) %s", request.getFQN(), 
						WordUtils.capitalize(changeData.getActivity()), request.getTitle());
				String threadingReferences = String.format("<%s-%s@onedev>", 
						changeData.getActivity().replace(' ', '-'), request.getUUID());
				mailManager.sendMailAsync(Lists.newArrayList(request.getSubmitter().getEmail()), 
						Lists.newArrayList(), Lists.newArrayList(), subject, 
						getHtmlBody(event, summary, event.getHtmlBody(), url, replyable, null), 
						getTextBody(event, summary, event.getTextBody(), url, replyable, null), 
						replyAddress, threadingReferences);
				notifiedUsers.add(request.getSubmitter());
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
					if (!notifiedEmailAddresses.stream().anyMatch(mentionedUser.getEmails()::contains)) {
						String subject = String.format("[Pull Request %s] (Mentioned You) %s", request.getFQN(), request.getTitle());
						String threadingReferences = String.format("<mentioned-%s@onedev>", request.getUUID());
						
						mailManager.sendMailAsync(Sets.newHashSet(mentionedUser.getEmail()), 
								Sets.newHashSet(), Sets.newHashSet(), subject, 
								getHtmlBody(event, summary, event.getHtmlBody(), url, replyable, null), 
								getTextBody(event, summary, event.getTextBody(), url, replyable, null),
								replyAddress, threadingReferences);
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
		} else if (!(event instanceof PullRequestMergePreviewCalculated || event instanceof PullRequestBuildEvent)) {
			notifyWatchers = true;
		}
		
		if (notifyWatchers) {
			Collection<User> bccUsers = new HashSet<>();
			
			for (PullRequestWatch watch: request.getWatches()) {
				Date visitDate = userInfoManager.getPullRequestVisitDate(watch.getUser(), request);
				if (watch.isWatching() 
						&& (visitDate == null || visitDate.before(event.getDate())) 
						&& (!(event instanceof PullRequestUpdated) || !watch.getUser().equals(request.getSubmitter()))
						&& !notifiedUsers.contains(watch.getUser())
						&& !notifiedEmailAddresses.stream().anyMatch(watch.getUser().getEmails()::contains)) {
					bccUsers.add(watch.getUser());
				}
			}

			if (!bccUsers.isEmpty()) {
				String subject = String.format("[Pull Request %s] (%s) %s", 
						request.getFQN(), (event instanceof PullRequestOpened)?"Opened":"Updated", request.getTitle());
				String threadingReferences = "<" + request.getUUID() + "@onedev>";
				Unsubscribable unsubscribable = new Unsubscribable(mailManager.getUnsubscribeAddress(request));
				String htmlBody = getHtmlBody(event, summary, event.getHtmlBody(), url, replyable, unsubscribable);
				String textBody = getTextBody(event, summary, event.getTextBody(), url, replyable, unsubscribable);
				mailManager.sendMailAsync(
						Lists.newArrayList(), Lists.newArrayList(),
						bccUsers.stream().map(User::getEmail).collect(Collectors.toList()), 
						subject, htmlBody, textBody, replyAddress, threadingReferences);
			}
		}				
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.isNew()) {
			if (event.getEntity() instanceof PullRequestReview) {
				PullRequestReview review = (PullRequestReview) event.getEntity();
				PullRequest request = review.getRequest();
				if (review.getResult() == null && !review.getUser().equals(SecurityUtils.getUser())) {
					pullRequestWatchManager.watch(request, review.getUser(), true);
					String url = urlManager.urlFor(request);
					String subject = String.format("[Pull Request %s] (Review Invitation) %s", 
							request.getFQN(), request.getTitle());
					String threadingReferences = "<review-invitation-" + request.getUUID() + "@onedev>";
					String replyAddress = mailManager.getReplyAddress(request);
					mailManager.sendMailAsync(Lists.newArrayList(review.getUser().getEmail()), 
							Lists.newArrayList(), Lists.newArrayList(), subject, 
							getHtmlBody(event, null, null, url, replyAddress != null, null), 
							getTextBody(event, null, null, url, replyAddress != null, null), 
							replyAddress, threadingReferences);
				}
			} else if (event.getEntity() instanceof PullRequestAssignment) {
				PullRequestAssignment assignment = (PullRequestAssignment) event.getEntity();
				PullRequest request = assignment.getRequest();
				if (!assignment.getUser().equals(SecurityUtils.getUser())) {
					pullRequestWatchManager.watch(request, assignment.getUser(), true);
					String url = urlManager.urlFor(request);
					String subject = String.format("[Pull Request %s] (Assigned) %s", request.getFQN(), request.getTitle());
					String threadingReferences = "<assigned-" + request.getUUID() + "@onedev>";
					String replyAddress = mailManager.getReplyAddress(request);
					mailManager.sendMailAsync(Lists.newArrayList(assignment.getUser().getEmail()), 
							Lists.newArrayList(), Lists.newArrayList(), subject, 
							getHtmlBody(event, null, null, url, replyAddress != null, null), 
							getTextBody(event, null, null, url, replyAddress != null, null), 
							replyAddress, threadingReferences);
				}
			}
		}
	}

} 
