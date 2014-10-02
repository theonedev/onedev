package com.pmease.gitplex.web.page.repository.pullrequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.component.markdown.MarkdownInput;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.PullRequest;
import static com.pmease.gitplex.core.model.PullRequestOperation.*;
import com.pmease.gitplex.core.model.PullRequestAudit;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.web.component.comment.event.CommentCollapsing;
import com.pmease.gitplex.web.component.comment.event.CommentRemoved;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.ApprovePullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.CommentPullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.DisapprovePullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.DiscardPullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.IntegratePullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.OpenPullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.PullRequestActivity;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.UpdatePullRequest;

@SuppressWarnings("serial")
public class RequestActivitiesPage extends RequestDetailPage {
	
	private RepeatingView activitiesView;
	
	public RequestActivitiesPage(PageParameters params) {
		super(params);
	}
	
	private Component newActivityItem(final String id, final PullRequestActivity activity) {
		final CommentPullRequest commentActivity;
		if (activity instanceof CommentPullRequest)
			commentActivity = (CommentPullRequest) activity;
		else
			commentActivity = null;
		
		final WebMarkupContainer row = new WebMarkupContainer(id) {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				
				if (event.getPayload() instanceof CommentRemoved) {
					CommentRemoved commentRemoved = (CommentRemoved) event.getPayload();
					remove();
					commentRemoved.getTarget().appendJavaScript(String.format("$('#%s').remove();", getMarkupId()));
				} else if (event.getPayload() instanceof CommentCollapsing) {
					Preconditions.checkNotNull(commentActivity);
					commentActivity.setCollapsed(true);
					Component row = newActivityItem(id, commentActivity);
					replaceWith(row);
					((CommentCollapsing) event.getPayload()).getTarget().add(row);
				}
			}
			
		};
		if (commentActivity != null && commentActivity.isCollapsed()) {
			PullRequestComment comment = commentActivity.getComment();

			Fragment fragment = new Fragment("activity", "collapsedCommentFrag", RequestActivitiesPage.this);

			fragment.add(new UserLink("user", new UserModel(comment.getUser()), AvatarMode.NAME));
			if (comment.getInlineInfo() != null)
				fragment.add(new Label("activity", "added inline comment on file '" + comment.getFile() + "'"));
			else 
				fragment.add(new Label("activity", "commented"));
			fragment.add(new AgeLabel("age", Model.of(comment.getDate())));
			
			fragment.add(new Label("detail", comment.getContent()));
			
			fragment.add(new AjaxLink<Void>("expand") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					row.replace(activity.render("activity"));
					commentActivity.setCollapsed(false);
					target.add(row);
				}
				
			});
			row.add(fragment);
		}

		row.setOutputMarkupId(true);
		
		row.add(new UserLink("avatar", new UserModel(activity.getUser()), AvatarMode.AVATAR));
		
		if (row.get("activity") == null) 
			row.add(activity.render("activity"));
		
		if (activity instanceof OpenPullRequest || activity instanceof CommentPullRequest)
			row.add(AttributeAppender.append("class", " discussion non-update"));
		else if (activity instanceof UpdatePullRequest)
			row.add(AttributeAppender.append("class", " non-discussion update"));
		else
			row.add(AttributeAppender.append("class", " non-discussion non-update"));
		
		row.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String cssClasses = "";
				if (activity instanceof CommentPullRequest) {
					CommentPullRequest commentActivity = (CommentPullRequest) activity;
					if (commentActivity.isCollapsed())
						cssClasses += " collapsed";
					if (commentActivity.getComment().isResolved())
						cssClasses += " resolved";
				} 
				return cssClasses;
			}
			
		}));
		
		return row;
	}
	
	private Component newActivitiesView() {
		activitiesView = new RepeatingView("requestActivities");
		activitiesView.setOutputMarkupId(true);
		
		PullRequest request = getPullRequest();
		List<PullRequestActivity> activities = new ArrayList<>();

		activities.add(new OpenPullRequest(request));

		for (PullRequestUpdate update: request.getUpdates())
			activities.add(new UpdatePullRequest(update));
		
		for (PullRequestComment comment: request.getComments()) 
			activities.add(new CommentPullRequest(comment));
		
		for (PullRequestAudit audit: request.getAudits()) {
			if (audit.getOperation() == INTEGRATE) {
				activities.add(new IntegratePullRequest(audit.getUser(), audit.getDate()));
			} else if (audit.getOperation() == DISCARD) { 
				activities.add(new DiscardPullRequest(audit.getUser(), audit.getDate()));
			} else if (audit.getOperation() == APPROVE) {
				activities.add(new ApprovePullRequest(audit.getRequest(), audit.getUser(), audit.getDate()));
			} else if (audit.getOperation() == DISAPPROVE) {
				activities.add(new DisapprovePullRequest(audit.getRequest(), audit.getUser(), audit.getDate()));
			} else {
				throw new IllegalStateException("Unexpected audit operation: " + audit.getOperation());
			}
		}
		
		Collections.sort(activities, new Comparator<PullRequestActivity>() {

			@Override
			public int compare(PullRequestActivity o1, PullRequestActivity o2) {
				if (o1.getDate().before(o2.getDate()))
					return -1;
				else if (o1.getDate().after(o2.getDate()))
					return 1;
				else if (o1 instanceof OpenPullRequest || o1 instanceof CommentPullRequest)
					return -1;
				else
					return 1;
			}
			
		});
		
		int index = 0;
		for (PullRequestActivity activity: activities) { 
			Component activityItem = newActivityItem(activitiesView.newChildId(), activity);
			if (index == activities.size()-1 || activities.get(index+1) instanceof CommentPullRequest)
				activityItem.add(AttributeAppender.append("class", " pre-discussion"));
			activitiesView.add(activityItem);
			index++;
		}
		
		return activitiesView;
	}
	
	@Override
	protected void onBeforeRender() {
		replace(newActivitiesView());
		
		super.onBeforeRender();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newActivitiesView());
		
		final WebMarkupContainer addComment = new WebMarkupContainer("addComment") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(GitPlex.getInstance(UserManager.class).getCurrent() != null);
			}
			
		};
		addComment.setOutputMarkupId(true);
		add(addComment);
		
		Form<?> form = new Form<Void>("form");
		addComment.add(form);
		
		final MarkdownInput input = new MarkdownInput("input", Model.of(""));
		input.setRequired(true);
		form.add(input);
		
		form.add(new AjaxSubmitLink("comment") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				PullRequestComment comment = new PullRequestComment();
				comment.setRequest(getPullRequest());
				comment.setUser(GitPlex.getInstance(UserManager.class).getCurrent());
				comment.setContent(input.getModelObject());
				GitPlex.getInstance(Dao.class).persist(comment);
				input.setModelObject("");
				
				target.add(addComment);
				
				Component lastActivityItem = activitiesView.get(activitiesView.size()-1);
				Component newActivityItem = newActivityItem(activitiesView.newChildId(), new CommentPullRequest(comment)); 
				activitiesView.add(newActivityItem);
				
				String script = String.format("$(\"<li id='%s' class='activity discussion'></li>\").insertAfter('#%s');", 
						newActivityItem.getMarkupId(), lastActivityItem.getMarkupId());
				target.prependJavaScript(script);
				target.add(newActivityItem);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

		});
	}

}
