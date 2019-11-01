package io.onedev.server.web.component.issue.activities.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.project.comment.ProjectCommentPanel;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
class IssueCommentedPanel extends GenericPanel<IssueComment> {

	private final DeleteCallback deleteCallback;
	
	public IssueCommentedPanel(String id, IModel<IssueComment> model, DeleteCallback deleteCallback) {
		super(id, model);
		this.deleteCallback = deleteCallback;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		UserIdent userIdent = UserIdent.of(getComment().getUser(), getComment().getUserName());
		add(new Label("user", userIdent.getName()));
		add(new Label("age", DateUtils.formatAge(getComment().getDate())));
		
		add(new ProjectCommentPanel("body") {

			@Override
			protected String getComment() {
				return IssueCommentedPanel.this.getComment().getContent();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				IssueCommentedPanel.this.getComment().setContent(comment);
				OneDev.getInstance(IssueCommentManager.class).save(IssueCommentedPanel.this.getComment());
			}

			@Override
			protected Project getProject() {
				return IssueCommentedPanel.this.getComment().getIssue().getProject();
			}

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(IssueCommentedPanel.this.getComment().getIssue().getProject(), 
						IssueCommentedPanel.this.getComment().getIssue().getUUID());
			}

			@Override
			protected boolean canModifyOrDeleteComment() {
				return SecurityUtils.canModifyOrDelete(IssueCommentedPanel.this.getComment());
			}

			@Override
			protected String getRequiredLabel() {
				return "Comment";
			}

			@Override
			protected ContentVersionSupport getContentVersionSupport() {
				return null;
			}

			@Override
			protected DeleteCallback getDeleteCallback() {
				return deleteCallback;
			}
			
		});

		setOutputMarkupId(true);
	}
	
	private IssueComment getComment() {
		return getModelObject();
	}
	
}
