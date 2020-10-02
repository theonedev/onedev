package io.onedev.server.web.component.issue.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.changedata.IssueDescriptionChangeData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromCodeCommentData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromIssueData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromPullRequestData;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.issue.activities.activity.IssueActivity;
import io.onedev.server.web.component.issue.activities.activity.IssueChangeActivity;
import io.onedev.server.web.component.issue.activities.activity.IssueCommentedActivity;
import io.onedev.server.web.component.issue.activities.activity.IssueOpenedActivity;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.project.comment.CommentInput;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.simple.security.LoginPage;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
public abstract class IssueActivitiesPanel extends Panel {

	private static final String COOKIE_SHOW_COMMENTS = "onedev.server.issue.showComments";
	
	private static final String COOKIE_SHOW_CHANGE_HISTORY = "onedev.server.issue.showChangeHistory";
 	
	private RepeatingView activitiesView;

	private boolean showComments = true;
	
	private boolean showChangeHistory = true;
	
	public IssueActivitiesPanel(String panelId) {
		super(panelId);
		
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_SHOW_COMMENTS);
		if (cookie != null)
			showComments = Boolean.valueOf(cookie.getValue());
		
		cookie = request.getCookie(COOKIE_SHOW_CHANGE_HISTORY);
		if (cookie != null)
			showChangeHistory = Boolean.valueOf(cookie.getValue());
	}

	@Override
	protected void onBeforeRender() {
		activitiesView = new RepeatingView("activities");
		addOrReplace(activitiesView);
		Issue issue = getIssue();

		for (IssueActivity activity: getActivities()) {
			if (issue.isVisitedAfter(activity.getDate())) {
				activitiesView.add(newActivityRow(activitiesView.newChildId(), activity));
			} else {
				Component row = newActivityRow(activitiesView.newChildId(), activity);
				row.add(AttributeAppender.append("class", "new"));
				activitiesView.add(row);
			}
		}		
		
		super.onBeforeRender();
	}
	
	private List<IssueActivity> getActivities() {
		List<IssueActivity> activities = new ArrayList<>();

		activities.add(new IssueOpenedActivity(getIssue()));

		List<IssueActivity> otherActivities = new ArrayList<>();
		if (showComments) {
			for (IssueComment comment: getIssue().getComments())  
				otherActivities.add(new IssueCommentedActivity(comment));
		}
		
		if (showChangeHistory) {
			for (IssueChange change: getIssue().getChanges()) {
				if (change.getData() instanceof IssueReferencedFromIssueData) {
					IssueReferencedFromIssueData referencedFromIssueData = (IssueReferencedFromIssueData) change.getData();
					Issue issue = OneDev.getInstance(IssueManager.class).get(referencedFromIssueData.getIssueId());
					if (issue != null)
						otherActivities.add(new IssueChangeActivity(change));
				} else if (change.getData() instanceof IssueReferencedFromPullRequestData) {
					IssueReferencedFromPullRequestData referencedFromPullRequestData = (IssueReferencedFromPullRequestData) change.getData();
					PullRequest request = OneDev.getInstance(PullRequestManager.class).get(referencedFromPullRequestData.getRequestId());
					if (request != null)
						otherActivities.add(new IssueChangeActivity(change));
				} else if (change.getData() instanceof IssueReferencedFromCodeCommentData) {
					IssueReferencedFromCodeCommentData referencedFromCodeCommentData = (IssueReferencedFromCodeCommentData) change.getData();
					CodeComment comment = OneDev.getInstance(CodeCommentManager.class).get(referencedFromCodeCommentData.getCommentId());
					if (comment != null)
						otherActivities.add(new IssueChangeActivity(change));
				} else if (!(change.getData() instanceof IssueDescriptionChangeData)) {
					otherActivities.add(new IssueChangeActivity(change));
				}
			}
		}
		
		otherActivities.sort((o1, o2) -> {
			if (o1.getDate().getTime()<o2.getDate().getTime())
				return -1;
			else if (o1.getDate().getTime()>o2.getDate().getTime())
				return 1;
			else
				return 1;
		});
		
		activities.addAll(otherActivities);
		
		return activities;
	}
	
	private Component newActivityRow(String id, IssueActivity activity) {
		WebMarkupContainer row = new WebMarkupContainer(id, Model.of(activity));
		row.setOutputMarkupId(true);
		String anchor = activity.getAnchor();
		if (anchor != null)
			row.setMarkupId(anchor);
		
		if (activity.getUser() != null) {
			row.add(new UserIdentPanel("avatar", activity.getUser(), Mode.AVATAR));
			row.add(AttributeAppender.append("class", "with-avatar"));
		} else {
			row.add(new WebMarkupContainer("avatar").setVisible(false));
		}

		row.add(activity.render("content", new DeleteCallback() {

			@Override
			public void onDelete(AjaxRequestTarget target) {
				row.remove();
				target.appendJavaScript(String.format("$('#%s').remove();", row.getMarkupId()));
			}
			
		}));
		
		row.add(AttributeAppender.append("class", activity.getClass().getSimpleName()));
		return row;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				updateActivities(handler);
			}
			
			private void updateActivities(IPartialPageRequestHandler handler) {
				@SuppressWarnings("deprecation")
				Component prevActivityRow = activitiesView.get(activitiesView.size()-1);
				IssueActivity lastActivity = (IssueActivity) prevActivityRow.getDefaultModelObject();
				List<IssueActivity> newActivities = new ArrayList<>();
				for (IssueActivity activity: getActivities()) {
					if (activity.getDate().getTime() > lastActivity.getDate().getTime())
						newActivities.add(activity);
				}

				for (IssueActivity activity: newActivities) {
					Component newActivityRow = newActivityRow(activitiesView.newChildId(), activity); 
					newActivityRow.add(AttributeAppender.append("class", "new"));
					activitiesView.add(newActivityRow);
					
					String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
							newActivityRow.getMarkupId(), prevActivityRow.getMarkupId());
					handler.prependJavaScript(script);
					handler.add(newActivityRow);
					prevActivityRow = newActivityRow;
				}
			}
			
			@Override
			public Collection<String> getObservables() {
				return Lists.newArrayList(Issue.getWebSocketObservable(getIssue().getId()));
			}
			
		});
		
		if (SecurityUtils.getUser() != null) {
			Fragment fragment = new Fragment("addComment", "addCommentFrag", this);
			fragment.setOutputMarkupId(true);
			CommentInput input = new CommentInput("comment", Model.of(""), false) {

				@Override
				protected AttachmentSupport getAttachmentSupport() {
					return new ProjectAttachmentSupport(getProject(), getIssue().getUUID(), 
							SecurityUtils.canManageIssues(getProject()));
				}

				@Override
				protected Project getProject() {
					return getIssue().getProject();
				}
				
				@Override
				protected List<User> getMentionables() {
					return OneDev.getInstance(UserManager.class).queryAndSort(getIssue().getParticipants());
				}
				
				@Override
				protected List<AttributeModifier> getInputModifiers() {
					return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a comment"));
				}
				
			};
			input.setRequired(true).setLabel(Model.of("Comment"));
			
			Form<?> form = new Form<Void>("form");
			form.add(new FencedFeedbackPanel("feedback", form));
			form.add(input);
			form.add(new AjaxSubmitLink("save") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);

					IssueComment comment = new IssueComment();
					comment.setContent(input.getModelObject());
					comment.setUser(SecurityUtils.getUser());
					comment.setDate(new Date());
					comment.setIssue(getIssue());
					OneDev.getInstance(IssueCommentManager.class).save(comment);
					
					input.clearMarkdown();
					
					@SuppressWarnings("deprecation")
					Component lastActivityRow = activitiesView.get(activitiesView.size()-1);
					Component newActivityRow = newActivityRow(activitiesView.newChildId(), new IssueCommentedActivity(comment)); 
					activitiesView.add(newActivityRow);
					
					String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
							newActivityRow.getMarkupId(), lastActivityRow.getMarkupId());
					target.prependJavaScript(script);
					target.add(newActivityRow);
					
					target.add(fragment);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					super.onError(target, form);
					target.add(form);
				}
				
			});
			form.setOutputMarkupId(true);
			fragment.add(form);
			add(fragment);
		} else {
			Fragment fragment = new Fragment("addComment", "loginToCommentFrag", this);
			fragment.add(new Link<Void>("login") {

				@Override
				public void onClick() {
					throw new RestartResponseAtInterceptPageException(LoginPage.class);
				}
				
			});
			add(fragment);
		}
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueActivitiesCssResourceReference()));
	}

	protected abstract Issue getIssue();

	public Component renderOptions(String componentId) {
		Fragment fragment = new Fragment(componentId, "optionsFrag", this);
		fragment.add(new AjaxLink<Void>("showComments") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (showComments)
					add(AttributeAppender.append("class", "active"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				showComments = !showComments;
				WebResponse response = (WebResponse) RequestCycle.get().getResponse();
				Cookie cookie = new Cookie(COOKIE_SHOW_COMMENTS, String.valueOf(showComments));
				cookie.setPath("/");
				cookie.setMaxAge(Integer.MAX_VALUE);
				response.addCookie(cookie);
				target.add(IssueActivitiesPanel.this);
				target.appendJavaScript(String.format("$('#%s').toggleClass('active');", getMarkupId()));
			}

		});
		
		fragment.add(new AjaxLink<Void>("showChangeHistory") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (showChangeHistory)
					add(AttributeAppender.append("class", "active"));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				showChangeHistory = !showChangeHistory;
				WebResponse response = (WebResponse) RequestCycle.get().getResponse();
				Cookie cookie = new Cookie(COOKIE_SHOW_CHANGE_HISTORY, String.valueOf(showChangeHistory));
				cookie.setPath("/");
				cookie.setMaxAge(Integer.MAX_VALUE);
				response.addCookie(cookie);
				target.add(IssueActivitiesPanel.this);
				target.appendJavaScript(String.format("$('#%s').toggleClass('active');", getMarkupId()));
			}

		});
				
		return fragment;
	}

}
