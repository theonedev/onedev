package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.project.comment.ProjectCommentPanel;
import io.onedev.server.web.page.project.pullrequests.detail.activities.SinceChangesLink;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
class PullRequestChangePanel extends GenericPanel<PullRequestChange> {

	public PullRequestChangePanel(String id, IModel<PullRequestChange> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequestChange change = getModelObject();

		if (change.getUser() != null) 
			add(new Label("user", change.getUser().getDisplayName()));
		else
			add(new WebMarkupContainer("user").setVisible(false));
		
		add(new Label("description", change.getData().getActivity()));
		add(new Label("age", DateUtils.formatAge(change.getDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(change.getDate()))));
		add(new SinceChangesLink("changes", new AbstractReadOnlyModel<PullRequest>() {

			@Override
			public PullRequest getObject() {
				return getChange().getRequest();
			}

		}, getChange().getDate()));
		
		ActivityDetail detail = getChange().getData().getActivityDetail();
		if (detail != null)
			add(detail.render("detail"));
		else
			add(new WebMarkupContainer("detail").setVisible(false));
		
		if (getChange().getData().getCommentAware() != null) {
			add(new ProjectCommentPanel("comment") {

				@Override
				protected String getComment() {
					return getChange().getData().getCommentAware().getComment();
				}

				@Override
				protected List<User> getMentionables() {
					return OneDev.getInstance(UserManager.class).queryAndSort(getChange().getRequest().getParticipants());
				}

				@Override
				protected void onSaveComment(AjaxRequestTarget target, String comment) {
					getChange().getData().getCommentAware().setComment(comment);
				}

				@Override
				protected Project getProject() {
					return getChange().getRequest().getProject();
				}

				@Override
				protected AttachmentSupport getAttachmentSupport() {
					return new ProjectAttachmentSupport(getProject(), getChange().getRequest().getUUID(), 
							SecurityUtils.canManagePullRequests(getProject()));
				}

				@Override
				protected boolean canModifyOrDeleteComment() {
					return SecurityUtils.canModifyOrDelete(getChange());
				}

				@Override
				protected String getRequiredLabel() {
					return null;
				}

				@Override
				protected ContentVersionSupport getContentVersionSupport() {
					return null;
				}

				@Override
				protected DeleteCallback getDeleteCallback() {
					return null;
				}
				
			});				
		} else {
			add(new WebMarkupContainer("comment").setVisible(false));
			if (detail == null)
				add(AttributeAppender.append("class", "no-body"));
		}
		
	}

	private PullRequestChange getChange() {
		return getModelObject();
	}
	
}
