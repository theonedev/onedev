package io.onedev.server.web.page.project.issues.issuedetail.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueCommentManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.comment.ProjectAttachmentSupport;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;
import io.onedev.server.web.page.project.issues.issuedetail.activities.activity.ActivityCallback;
import io.onedev.server.web.page.project.issues.issuedetail.activities.activity.ChangedActivity;
import io.onedev.server.web.page.project.issues.issuedetail.activities.activity.CommentedActivity;
import io.onedev.server.web.page.project.issues.issuedetail.activities.activity.IssueActivity;
import io.onedev.server.web.page.project.issues.issuedetail.activities.activity.OpenedActivity;
import io.onedev.server.web.page.security.LoginPage;

@SuppressWarnings("serial")
public class IssueActivitiesPage extends IssueDetailPage {

	private RepeatingView activitiesView;
	
	public IssueActivitiesPage(PageParameters params) {
		super(params);
	}

	private List<IssueActivity> getActivities() {
		List<IssueActivity> activities = new ArrayList<>();

		activities.add(new OpenedActivity(getIssue()));

		for (IssueComment comment: getIssue().getComments())  
			activities.add(new CommentedActivity(comment));
		
		for (IssueChange change: getIssue().getChanges())
			activities.add(new ChangedActivity(change));
		
		activities.sort((o1, o2) -> {
			if (o1.getDate().getTime()<o2.getDate().getTime())
				return -1;
			else if (o1.getDate().getTime()>o2.getDate().getTime())
				return 1;
			else
				return 1;
		});
		
		return activities;
	}
	
	private Component newActivityRow(String id, IssueActivity activity) {
		WebMarkupContainer row = new WebMarkupContainer(id, Model.of(activity));
		row.setOutputMarkupId(true);
		String anchor = activity.getAnchor();
		if (anchor != null)
			row.setMarkupId(anchor);
		
		if (row.get("content") == null) {
			row.add(activity.render("content", new ActivityCallback() {

				@Override
				public void onDelete(AjaxRequestTarget target) {
					row.remove();
					target.appendJavaScript(String.format("$('#%s').remove();", row.getMarkupId()));
				}
				
			}));
		}
		
		row.add(new AvatarLink("avatar", activity.getUser()));

		row.add(AttributeAppender.append("class", activity.getClass().getSimpleName()));
		return row;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(activitiesView = new RepeatingView("activities"));
		activitiesView.setOutputMarkupId(true);
		
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
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, String observable) {
				updateActivities(handler);
			}
			
			@Override
			public void onConnectionOpened(IPartialPageRequestHandler handler) {
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
		
		if (getLoginUser() != null) {
			Fragment fragment = new Fragment("addComment", "addCommentFrag", this);
			fragment.setOutputMarkupId(true);
			CommentInput input = new CommentInput("comment", Model.of(""), false) {

				@Override
				protected AttachmentSupport getAttachmentSupport() {
					return new ProjectAttachmentSupport(getProject(), getIssue().getUUID());
				}

				@Override
				protected Project getProject() {
					return IssueActivitiesPage.this.getProject();
				}
				
				@Override
				protected List<AttributeModifier> getInputModifiers() {
					return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a comment"));
				}
				
			};
			input.setRequired(true).setLabel(Model.of("Comment"));
			
			Form<?> form = new Form<Void>("form");
			form.add(new NotificationPanel("feedback", form));
			form.add(input);
			form.add(new AjaxSubmitLink("save") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);

					IssueComment comment = new IssueComment();
					comment.setContent(input.getModelObject());
					comment.setUser(getLoginUser());
					comment.setDate(new Date());
					comment.setIssue(getIssue());
					OneDev.getInstance(IssueCommentManager.class).save(comment);
					
					input.setModelObject("");

					@SuppressWarnings("deprecation")
					Component lastActivityRow = activitiesView.get(activitiesView.size()-1);
					Component newActivityRow = newActivityRow(activitiesView.newChildId(), new CommentedActivity(comment)); 
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
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueActivitiesResourceReference()));
	}

}
