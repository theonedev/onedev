package io.onedev.server.web.page.project.pullrequests.detail.activities;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDescriptionChangeData;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.activity.PullRequestChangeActivity;
import io.onedev.server.web.page.project.pullrequests.detail.activities.activity.PullRequestCommentActivity;
import io.onedev.server.web.page.project.pullrequests.detail.activities.activity.PullRequestUpdateActivity;
import io.onedev.server.web.page.security.LoginPage;

public class PullRequestActivitiesPage extends PullRequestDetailPage {
	
	private static final String COOKIE_SHOW_COMMENTS = "onedev.server.pullRequest.showComments";
	
	private static final String COOKIE_SHOW_COMMITS = "onedev.server.pullRequest.showCommits";
	
	private static final String COOKIE_SHOW_CHANGE_HISTORY = "onedev.server.pullRequest.showChangeHistory";
	
	private boolean showComments = true;
	
	private boolean showCommits = true;
	
	private boolean showChangeHistory = true;
	
	private WebMarkupContainer container;
	
	private RepeatingView activitiesView;
	
	private Component showCommentsLink;	
	
	public PullRequestActivitiesPage(PageParameters params) {
		super(params);

		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_SHOW_COMMENTS);
		if (cookie != null)
			showComments = Boolean.valueOf(cookie.getValue());
		
		cookie = request.getCookie(COOKIE_SHOW_COMMITS);
		if (cookie != null)
			showCommits = Boolean.valueOf(cookie.getValue());
		
		cookie = request.getCookie(COOKIE_SHOW_CHANGE_HISTORY);
		if (cookie != null)
			showChangeHistory = Boolean.valueOf(cookie.getValue());
	}
	
	private Component newActivityRow(String id, PullRequestActivity activity) {
		var row = activity.render(id);		
		row.add(AttributeAppender.append("class", activity.getClass().getSimpleName()));
		if (activity instanceof PullRequestCommentActivity) 
			row.add(AttributeAppender.append("class", "comment"));
		else 
			row.add(AttributeAppender.append("class", "non-comment"));
		row.setDefaultModel(Model.of(activity));
		row.setOutputMarkupId(true);		
		return row;
	}
	
	private List<PullRequestActivity> getActivities() {
		PullRequest request = getPullRequest();
		List<PullRequestActivity> activities = new ArrayList<>();
		
		List<PullRequestActivity> otherActivities = new ArrayList<>();
		
		if (showComments) {
			for (PullRequestComment comment: request.getComments()) 
				otherActivities.add(new PullRequestCommentActivity(comment));
		}

		if (showCommits) {
			for (PullRequestUpdate update: request.getUpdates())
				otherActivities.add(new PullRequestUpdateActivity(update));
		}

		if (showChangeHistory) {
			for (PullRequestChange change: getPullRequest().getChanges()) {
				if (!(change.getData() instanceof ReferencedFromAware) 
						&& !(change.getData() instanceof PullRequestDescriptionChangeData)) {
					if (change.getData() instanceof ReferencedFromAware) {
						var referencedFromAware = (ReferencedFromAware<?>) change.getData();
						if (ReferencedFromAware.canDisplay(referencedFromAware))
							otherActivities.add(new PullRequestChangeActivity(change));		
					} else {
						otherActivities.add(new PullRequestChangeActivity(change));
					}							
				}
			}
		}
		
		otherActivities.sort(Comparator.comparingLong(o -> o.getDate().getTime()));
		
		activities.addAll(otherActivities);
		
		return activities;
	}

	private Component newSinceChangesRow(String id, Date sinceDate) {
		var row = new Fragment(id, "sinceChangesRowFrag", this);
		row.add(new SinceChangesLink("sinceChanges", requestModel, sinceDate));		
		row.add(AttributeAppender.append("class", "since-changes"));
		row.setOutputMarkupId(true);					
		return row;
	}

	private BuildService getBuildService() {
		return 	OneDev.getInstance(BuildService.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(container = new WebMarkupContainer("activities") {

			@Override
			protected void onBeforeRender() {
				addOrReplace(activitiesView = new RepeatingView("activities"));
				
				List<PullRequestActivity> activities = getActivities();
				
				Collection<ObjectId> commitIds = new HashSet<>();
				for (PullRequestActivity activity: activities) {
					if (activity instanceof PullRequestUpdateActivity) {
						PullRequestUpdateActivity updatedActivity = (PullRequestUpdateActivity) activity;
						commitIds.addAll(updatedActivity.getUpdate().getCommits()
								.stream().map(it->it.copy()).collect(Collectors.toSet()));
					}
				}
				
				PullRequest request = getPullRequest();
				Project project = request.getTargetProject();
				project.cacheCommitStatuses(getBuildService().queryStatus(project, commitIds));
					
				List<PullRequestActivity> oldActivities = new ArrayList<>();
				List<PullRequestActivity> newActivities = new ArrayList<>();
				
				for (PullRequestActivity activity: activities) {
					if (request.isVisitedAfter(activity.getDate())) 
						oldActivities.add(activity);
					else 
						newActivities.add(activity);
				}

				for (PullRequestActivity activity: oldActivities) {
					activitiesView.add(newActivityRow(activitiesView.newChildId(), activity));
				}

				if (!oldActivities.isEmpty() && !newActivities.isEmpty()) {
					Date sinceDate = new DateTime(newActivities.iterator().next().getDate()).minusSeconds(1).toDate();
					if (newActivities.stream().anyMatch(it -> it instanceof PullRequestUpdateActivity))
						activitiesView.add(newSinceChangesRow(activitiesView.newChildId(), sinceDate));
				}
				
				for (PullRequestActivity activity: newActivities) {
					Component row = newActivityRow(activitiesView.newChildId(), activity);
					row.add(AttributeAppender.append("class", "new"));
					activitiesView.add(row);
				}
				
				super.onBeforeRender();
			}
			
		});
		container.setOutputMarkupId(true);
				
		if (getLoginUser() != null) {
			Fragment fragment = new Fragment("addComment", "addCommentFrag", this);
			fragment.setOutputMarkupId(true);

			Form<?> form = new Form<Void>("form");
			fragment.add(form);
			
			CommentInput input = new CommentInput("input", Model.of(""), false) {

				@Override
				protected AttachmentSupport getAttachmentSupport() {
					return new ProjectAttachmentSupport(getProject(), getPullRequest().getUUID(), 
							SecurityUtils.canManagePullRequests(getProject()));
				}

				@Override
				protected Project getProject() {
					return PullRequestActivitiesPage.this.getProject();
				}

				@Nullable
				@Override
				protected String getAutosaveKey() {
					return "pull-request:" + getPullRequest().getId() + ":new-comment";
				}

				@Override
				protected List<User> getParticipants() {
					return getPullRequest().getParticipants();
				}
				
				@Override
				protected List<Behavior> getInputBehaviors() {
					return Lists.newArrayList(AttributeModifier.replace("placeholder", _T("Leave a comment")));
				}
				
			};
			input.setRequired(true).setLabel(Model.of(_T("Comment")));
			form.add(input);
			
			form.add(new FencedFeedbackPanel("feedback", form));
			
			form.add(new AjaxSubmitLink("save") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);

					String content = input.getModelObject();
					if (content.length() > PullRequestComment.MAX_CONTENT_LEN) {
						form.error(_T("Comment too long"));
						target.add(form);
					} else {
						PullRequestComment comment = new PullRequestComment();
						comment.setRequest(getPullRequest());
						comment.setUser(getLoginUser());
						comment.setContent(input.getModelObject());
						OneDev.getInstance(PullRequestCommentService.class).create(comment, new ArrayList<>());
						
						if (showComments) {
							((BasePage) getPage()).notifyObservableChange(target,
									PullRequest.getChangeObservable(getPullRequest().getId()));
							target.add(fragment);
						} else {
							showComments = true;
							target.add(container);
							target.add(showCommentsLink);
						}
						input.clearMarkdown();
						input.focus(target);
					}
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					super.onError(target, form);
					target.add(form);
				}

			});
			container.add(fragment);
		} else {
			Fragment fragment = new Fragment("addComment", "loginToCommentFrag", this);
			fragment.add(new Link<Void>("login") {

				@Override
				public void onClick() {
					throw new RestartResponseAtInterceptPageException(LoginPage.class);
				}
				
			});
			container.add(fragment);
		}
		
		add(new ChangeObserver() {

			@Override
			public Collection<String> findObservables() {
				return Sets.newHashSet(PullRequest.getChangeObservable(getPullRequest().getId()));
			}

			@SuppressWarnings("deprecation")
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
				Component prevActivityRow = null;
				PullRequestActivity lastActivity = null;
				if (activitiesView.size() > 0) {
					prevActivityRow = activitiesView.get(activitiesView.size()-1);
					lastActivity = (PullRequestActivity) prevActivityRow.getDefaultModelObject();
				}
				
				List<PullRequestActivity> newActivities = new ArrayList<>();
				for (PullRequestActivity activity: getActivities()) {
					if (lastActivity == null || activity.getDate().after(lastActivity.getDate()))
						newActivities.add(activity);
				}

				Component sinceChangesRow = null;
				for (Component row: activitiesView) {
					if (row.getDefaultModelObject() == null) {
						sinceChangesRow = row;
						break;
					}
				}

				if (sinceChangesRow == null && !newActivities.isEmpty() 
						&& newActivities.stream().anyMatch(it -> it instanceof PullRequestUpdateActivity)) {
					Date sinceDate = new DateTime(newActivities.iterator().next().getDate()).minusSeconds(1).toDate();
					sinceChangesRow = newSinceChangesRow(activitiesView.newChildId(), sinceDate);
					activitiesView.add(sinceChangesRow);
					String script;
					if (prevActivityRow != null) {
						script = String.format("$(\"<li id='%s'></li>\").insertAfter('#%s');", 
								sinceChangesRow.getMarkupId(), prevActivityRow.getMarkupId());
					} else {
						script = String.format("$(\"<li id='%s'></li>\").prependTo($('#%s'));", 
								sinceChangesRow.getMarkupId(), container.getMarkupId());
					}
					handler.prependJavaScript(script);
					handler.add(sinceChangesRow);
					prevActivityRow = sinceChangesRow;
				}
							
				Collection<ObjectId> commitIds = new HashSet<>();

				for (PullRequestActivity activity: newActivities) {
					Component newActivityRow = newActivityRow(activitiesView.newChildId(), activity); 
					newActivityRow.add(AttributeAppender.append("class", "new"));
					activitiesView.add(newActivityRow);
					
					String script;
					if (prevActivityRow != null) {
						script = String.format("$(\"<li id='%s'></li>\").insertAfter('#%s');", 
								newActivityRow.getMarkupId(), prevActivityRow.getMarkupId());
					} else {
						script = String.format("$(\"<li id='%s'></li>\").prependTo($('#%s'));", 
								newActivityRow.getMarkupId(), container.getMarkupId());
					}
					handler.prependJavaScript(script);
					handler.add(newActivityRow);
					
					if (activity instanceof PullRequestUpdateActivity) {
						handler.appendJavaScript("$('li.since-changes').addClass('visible');");
						PullRequestUpdateActivity updatedActivity = (PullRequestUpdateActivity) activity;
						commitIds.addAll(updatedActivity.getUpdate().getCommits()
								.stream().map(it->it.copy()).collect(Collectors.toSet()));
					}
					prevActivityRow = newActivityRow;
				}
				
				PullRequest request = getPullRequest();
				Project project = request.getTargetProject();
				project.cacheCommitStatuses(getBuildService().queryStatus(project, commitIds));
			}
			
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new PullRequestActivitiesCssResourceReference()));
	}

	public Component renderOptions(String componentId) {
		Fragment fragment = new Fragment(componentId, "optionsFrag", this);
		fragment.add(showCommentsLink = new AjaxLink<Void>("showComments") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {
					@Override
					public String getObject() {
						return showComments?"active":""	;
					}
				}));
				setOutputMarkupId(true);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				showComments = !showComments;
				WebResponse response = (WebResponse) RequestCycle.get().getResponse();
				Cookie cookie = new Cookie(COOKIE_SHOW_COMMENTS, String.valueOf(showComments));
				cookie.setPath("/");
				cookie.setMaxAge(Integer.MAX_VALUE);
				response.addCookie(cookie);
				target.add(container);
				target.appendJavaScript(String.format("$('#%s').toggleClass('active');", getMarkupId()));
			}
			
		});
		
		fragment.add(new AjaxLink<Void>("showCommits") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (showCommits)
					add(AttributeAppender.append("class", "active"));
				setOutputMarkupId(true);
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				showCommits = !showCommits;
				WebResponse response = (WebResponse) RequestCycle.get().getResponse();
				Cookie cookie = new Cookie(COOKIE_SHOW_COMMITS, String.valueOf(showCommits));
				cookie.setPath("/");
				cookie.setMaxAge(Integer.MAX_VALUE);
				response.addCookie(cookie);
				target.add(container);
				target.appendJavaScript(String.format("$('#%s').toggleClass('active');", getMarkupId()));
			}

		});
		
		fragment.add(new AjaxLink<Void>("showChangeHistory") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (showChangeHistory)
					add(AttributeAppender.append("class", "active"));
				setOutputMarkupId(true);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				showChangeHistory = !showChangeHistory;
				WebResponse response = (WebResponse) RequestCycle.get().getResponse();
				Cookie cookie = new Cookie(COOKIE_SHOW_CHANGE_HISTORY, String.valueOf(showChangeHistory));
				cookie.setPath("/");
				cookie.setMaxAge(Integer.MAX_VALUE);
				response.addCookie(cookie);
				target.add(container);
				target.appendJavaScript(String.format("$('#%s').toggleClass('active');", getMarkupId()));
			}

		});
		
		return fragment;
	}
}
