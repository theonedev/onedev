package io.onedev.server.web.component.issue.activities.activity;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.comment.CommentPanel;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.DeleteCallback;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.onedev.server.security.SecurityUtils.canManageIssues;
import static io.onedev.server.util.EmailAddressUtils.describe;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

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

		add(new Label("user", getComment().getUser().getDisplayName()));
		add(new Label("age", DateUtils.formatAge(getComment().getDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(getComment().getDate()))));
		if (getComment().getOnBehalfOf() != null)
			add(new Label("onBehalfOf", " on behalf of <b>" + escapeHtml5(describe(getComment().getOnBehalfOf(), canManageIssues(getComment().getIssue().getProject()))) + "</b>").setEscapeModelStrings(false));
		else
			add(new WebMarkupContainer("onBehalfOf").setVisible(false));
		
		add(new WebMarkupContainer("anchor") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("href", "#" + getComment().getAnchor());
			}
			
		});
		
		add(new CommentPanel("body") {

			@Override
			protected String getComment() {
				return IssueCommentedPanel.this.getComment().getContent();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				if (comment.length() > IssueComment.MAX_CONTENT_LEN)
					throw new ExplicitException("Comment too long");
				var entity = IssueCommentedPanel.this.getComment();
				entity.setContent(comment);
				OneDev.getInstance(IssueCommentManager.class).update(entity);
				notifyIssueChange(target);
			}

			@Override
			protected Project getProject() {
				return IssueCommentedPanel.this.getComment().getIssue().getProject();
			}

			@Override
			protected List<User> getParticipants() {
				return IssueCommentedPanel.this.getComment().getIssue().getParticipants();
			}
			
			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), 
						IssueCommentedPanel.this.getComment().getIssue().getUUID(), 
						canManageIssues(getProject()));
			}

			@Override
			protected boolean canManageComment() {
				return SecurityUtils.canModifyOrDelete(IssueCommentedPanel.this.getComment());
			}

			@Override
			protected String getRequiredLabel() {
				return "Comment";
			}

			@Override
			protected ContentVersionSupport getContentVersionSupport() {
				return () -> 0;
			}

			@Override
			protected DeleteCallback getDeleteCallback() {
				return target -> {
					notifyIssueChange(target);
					deleteCallback.onDelete(target);
				};
			}

			@Nullable
			@Override
			protected String getAutosaveKey() {
				return "issue-comment:" + IssueCommentedPanel.this.getComment().getId();
			}
		});

		setOutputMarkupId(true);
	}
	
	private IssueComment getComment() {
		return getModelObject();
	}
	
	private void notifyIssueChange(AjaxRequestTarget target) {
		((BasePage)getPage()).notifyObservablesChange(target, getComment().getIssue().getChangeObservables(false));
	}	
}
