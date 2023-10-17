package io.onedev.server.web.component.issue.activities.activity;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.facade.UserCache;
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

import java.util.ArrayList;
import java.util.List;

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
			protected List<User> getMentionables() {
				UserCache cache = OneDev.getInstance(UserManager.class).cloneCache();		
				List<User> users = new ArrayList<>(cache.getUsers());
				users.sort(cache.comparingDisplayName(IssueCommentedPanel.this.getComment().getIssue().getParticipants()));
				return users;
			}
			
			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), 
						IssueCommentedPanel.this.getComment().getIssue().getUUID(), 
						SecurityUtils.canManageIssues(getProject()));
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
