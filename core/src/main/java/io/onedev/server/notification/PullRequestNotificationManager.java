package io.onedev.server.notification;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.CallbackException;
import org.hibernate.type.Type;

import com.google.common.collect.Lists;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.cache.UserInfoManager;
import io.onedev.server.entitymanager.PullRequestWatchManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.event.MarkdownAware;
import io.onedev.server.event.pullrequest.PullRequestChangeEvent;
import io.onedev.server.event.pullrequest.PullRequestBuildEvent;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentCreated;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentEvent;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentReplied;
import io.onedev.server.event.pullrequest.PullRequestCommentCreated;
import io.onedev.server.event.pullrequest.PullRequestDeleted;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.event.pullrequest.PullRequestMergePreviewCalculated;
import io.onedev.server.event.pullrequest.PullRequestUpdated;
import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.persistence.PersistListener;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.QueryWatchBuilder;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.util.markdown.MarkdownManager;
import io.onedev.server.util.markdown.MentionParser;

@Singleton
public class PullRequestNotificationManager implements PersistListener {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final MarkdownManager markdownManager;
	
	private final PullRequestWatchManager pullRequestWatchManager;
	
	private final UserInfoManager userInfoManager;
	
	@Inject
	public PullRequestNotificationManager(MailManager mailManager, UrlManager urlManager, 
			MarkdownManager markdownManager, PullRequestWatchManager pullRequestWatchManager, 
			UserInfoManager userInfoManager) {
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.markdownManager = markdownManager;
		this.pullRequestWatchManager = pullRequestWatchManager;
		this.userInfoManager = userInfoManager;
	}
	
	@Transactional
	@Listen
	public void on(PullRequestEvent event) {
		if (!(event instanceof PullRequestDeleted)) {
			PullRequest request = event.getRequest();
			User user = event.getUser();
			
			for(Map.Entry<User, Boolean> entry: new QueryWatchBuilder<PullRequest>() {

				@Override
				protected PullRequest getEntity() {
					return request;
				}

				@Override
				protected Collection<? extends QuerySetting<?>> getQuerySettings() {
					return request.getTargetProject().getPullRequestQuerySettings();
				}

				@Override
				protected EntityQuery<PullRequest> parse(String queryString) {
					return PullRequestQuery.parse(request.getTargetProject(), queryString, true);
				}

				@Override
				protected NamedQuery getSavedProjectQuery(String name) {
					return request.getTargetProject().getSavedPullRequestQuery(name);
				}
				
			}.getWatches().entrySet()) {
				watch(request, entry.getKey(), entry.getValue());
			};
			
			if (user != null)
				watch(request, user, true);
			
			Collection<User> notifiedUsers = new HashSet<>();
			if (event instanceof MarkdownAware && (!(event instanceof PullRequestCodeCommentEvent) || !((PullRequestCodeCommentEvent)event).isDerived())) {
				MarkdownAware markdownAware = (MarkdownAware) event;
				String markdown = markdownAware.getMarkdown();
				if (markdown != null) {
					String rendered = markdownManager.render(markdown);
					Collection<User> mentionUsers = new MentionParser().parseMentions(rendered);
					if (!mentionUsers.isEmpty()) {
						for (User mentionedUser: mentionUsers)
							watch(request, mentionedUser, true);
						
						String url;
						if (event instanceof PullRequestCommentCreated)
							url = urlManager.urlFor(((PullRequestCommentCreated)event).getComment());
						else if (event instanceof PullRequestChangeEvent) 
							url = urlManager.urlFor(((PullRequestChangeEvent)event).getChange());
						else if (event instanceof PullRequestCodeCommentCreated)
							url = urlManager.urlFor(((PullRequestCodeCommentCreated)event).getComment(), request);
						else if (event instanceof PullRequestCodeCommentReplied)
							url = urlManager.urlFor(((PullRequestCodeCommentReplied)event).getReply(), request);
						else 
							url = urlManager.urlFor(request);
						
						String subject = String.format("You are mentioned in pull request #%d - %s", 
								request.getNumber(), request.getTitle());
						String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
						
						mailManager.sendMailAsync(mentionUsers.stream().map(User::getEmail).collect(Collectors.toList()), 
								subject, body);
						notifiedUsers.addAll(mentionUsers);
					}
				}
			} 
			
			if (event instanceof PullRequestChangeEvent) {
				PullRequestChangeEvent actionEvent = (PullRequestChangeEvent) event;
				PullRequestChangeData actionData = actionEvent.getChange().getData();
				String subject = null;
				if (actionData instanceof PullRequestApproveData) {
					subject = String.format(user.getDisplayName() + " approved pull request #%d - %s", 
							request.getNumber(), request.getTitle());
				} else if (actionData instanceof PullRequestRequestedForChangesData) {
					subject = String.format(user.getDisplayName() + " requested changes for pull request #%d - %s", 
							request.getNumber(), request.getTitle());
				}
				if (request.getSubmitter() != null && subject != null) { 
					String url = urlManager.urlFor(request);
					String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
					mailManager.sendMailAsync(Lists.newArrayList(request.getSubmitter().getEmail()), subject, body);
					notifiedUsers.add(request.getSubmitter());
				}
			} else if (event instanceof PullRequestMergePreviewCalculated && request.getMergePreview() != null 
					&& request.getMergePreview().getMerged() == null) {
				String subject = String.format("Merge conflicts in pull request #%d - %s", 
						request.getNumber(), request.getTitle());
				String url = urlManager.urlFor(request);
				String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
				mailManager.sendMailAsync(Lists.newArrayList(request.getSubmitter().getEmail()), subject, body);
				notifiedUsers.add(request.getSubmitter());
			} else if (event instanceof PullRequestBuildEvent) {
				Build build = ((PullRequestBuildEvent) event).getBuild();
				if (build.getStatus() == Build.Status.IN_ERROR 
						|| build.getStatus() == Build.Status.FAILED 
						|| build.getStatus() == Build.Status.CANCELLED
						|| build.getStatus() == Build.Status.TIMED_OUT) {
					String subject = String.format("Failed to build pull request #%d - %s", 
							request.getNumber(), request.getTitle());
					String url = urlManager.urlFor(request);
					String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
					mailManager.sendMailAsync(Lists.newArrayList(request.getSubmitter().getEmail()), subject, body);
					notifiedUsers.add(request.getSubmitter());
				}
			}
			
			boolean notifyWatchers = false;
			if (event instanceof PullRequestChangeEvent) {
				PullRequestChangeData actionData = ((PullRequestChangeEvent) event).getChange().getData();
				if (actionData instanceof PullRequestApproveData || actionData instanceof PullRequestRequestedForChangesData 
						|| actionData instanceof PullRequestMergeData || actionData instanceof PullRequestDiscardData
						|| actionData instanceof PullRequestReopenData) {
					notifyWatchers = true;
				}
			} else if (!(event instanceof PullRequestMergePreviewCalculated || event instanceof PullRequestBuildEvent)) {
				notifyWatchers = true;
			}
			
			if (notifyWatchers) {
				Collection<User> usersToNotify = new HashSet<>();
				
				for (PullRequestWatch watch: request.getWatches()) {
					Date visitDate = userInfoManager.getPullRequestVisitDate(watch.getUser(), request);
					if (watch.isWatching() 
							&& !userInfoManager.isNotified(watch.getUser(), watch.getRequest()) 
							&& !watch.getUser().equals(event.getUser()) 
							&& (visitDate == null || visitDate.getTime()<event.getDate().getTime()) 
							&& (!(event instanceof PullRequestUpdated) || !watch.getUser().equals(request.getSubmitter()))
							&& !notifiedUsers.contains(watch.getUser())) {
						usersToNotify.add(watch.getUser());
						userInfoManager.setPullRequestNotified(watch.getUser(), watch.getRequest(), true);
						pullRequestWatchManager.save(watch);
					}
				}

				if (!usersToNotify.isEmpty()) {
					String url = urlManager.urlFor(request);
					String subject = String.format("New activities in pull request #%d - %s", request.getNumber(), request.getTitle());
					String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
					mailManager.sendMailAsync(usersToNotify.stream().map(User::getEmail).collect(Collectors.toList()), subject, body);
				}
			}				
		}
	}

	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		return false;
	}

	@Transactional
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) throws CallbackException {
		if (entity instanceof PullRequestReview) {
			PullRequestReview review = (PullRequestReview) entity;
			if (review.getExcludeDate() == null && review.getResult() == null) {
				for (int i=0; i<propertyNames.length; i++) {
					if (propertyNames[i].equals(PullRequestReview.PATH_RESULT) && previousState[i] != null) {
						inviteToReview(review);
						break;
					}
				}
			}
		}
		return false;
	}
	
	private void watch(PullRequest request, User user, boolean watching) {
		PullRequestWatch watch = (PullRequestWatch) request.getWatch(user, true);
		if (watch.isNew()) {
			watch.setWatching(watching);
			pullRequestWatchManager.save(watch);
		}
	}

	private void inviteToReview(PullRequestReview review) {
		PullRequest request = review.getRequest();
		watch(request, review.getUser(), true);
		String url = urlManager.urlFor(request);
		String subject = String.format("You are invited to review pull request #%d - %s", 
				request.getNumber(), request.getTitle());
		String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
		
		mailManager.sendMailAsync(Lists.newArrayList(review.getUser().getEmail()), subject, body);
	}

	@Transactional
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		if (entity instanceof PullRequestReview) {
			PullRequestReview review = (PullRequestReview) entity;
			if (review.getExcludeDate() == null && review.getResult() == null)
				inviteToReview(review);
		}
		return false;
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
	}
	
}
