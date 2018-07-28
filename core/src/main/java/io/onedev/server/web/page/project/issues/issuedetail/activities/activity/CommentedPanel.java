package io.onedev.server.web.page.project.issues.issuedetail.activities.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueCommentManager;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.projectcomment.ProjectCommentPanel;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
class CommentedPanel extends GenericPanel<IssueComment> {

	private final DeleteCallback deleteCallback;
	
	public CommentedPanel(String id, IModel<IssueComment> model, DeleteCallback deleteCallback) {
		super(id, model);
		this.deleteCallback = deleteCallback;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new UserLink("user", User.getForDisplay(getComment().getUser(), getComment().getUserName())));
		add(new Label("age", DateUtils.formatAge(getComment().getDate())));
		
		add(new ProjectCommentPanel("body") {

			@Override
			protected String getComment() {
				return CommentedPanel.this.getComment().getContent();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				CommentedPanel.this.getComment().setContent(comment);
				OneDev.getInstance(IssueCommentManager.class).save(CommentedPanel.this.getComment());
			}

			@Override
			protected Project getProject() {
				return CommentedPanel.this.getComment().getIssue().getProject();
			}

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(CommentedPanel.this.getComment().getIssue().getProject(), 
						CommentedPanel.this.getComment().getIssue().getUUID());
			}

			@Override
			protected boolean canManageComment() {
				return SecurityUtils.canModify(CommentedPanel.this.getComment().getIssue());
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
