package io.onedev.server.web.component.issue.activities.activity;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.project.comment.ProjectCommentPanel;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
class IssueChangePanel extends GenericPanel<IssueChange> {

	public IssueChangePanel(String id, IModel<IssueChange> model) {
		super(id, model);
	}
	
	private IssueChange getChange() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String activity = getChange().getData().getActivity();
		if (getChange().getUser() != null) {
			add(new Label("user", getChange().getUser().getDisplayName()));
		} else {
			add(new WebMarkupContainer("user").setVisible(false));
			activity = StringUtils.capitalize(activity);
		}

		add(new Label("description", activity));
		
		add(new Label("age", DateUtils.formatAge(getChange().getDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(getChange().getDate()))));
		
		ActivityDetail detail = getChange().getData().getActivityDetail();
		if (detail != null)
			add(detail.render("detail"));
		else
			add(new WebMarkupContainer("detail").setVisible(false));
		
		add(new ProjectCommentPanel("comment") {

			@Override
			protected String getComment() {
				return getChange().getComment();
			}

			@Override
			protected List<User> getMentionables() {
				return OneDev.getInstance(UserManager.class).queryAndSort(getChange().getIssue().getParticipants());
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				getChange().setComment(comment);
				OneDev.getInstance(IssueChangeManager.class).save(getChange());
			}

			@Override
			protected Project getProject() {
				return getChange().getIssue().getProject();
			}

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), getChange().getIssue().getUUID(), 
						SecurityUtils.canManageIssues(getProject()));
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
	}

}
