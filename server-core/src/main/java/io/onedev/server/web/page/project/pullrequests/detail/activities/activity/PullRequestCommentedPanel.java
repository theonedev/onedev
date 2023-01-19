package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.project.comment.CommentPanel;
import io.onedev.server.web.page.project.pullrequests.detail.activities.SinceChangesLink;
import io.onedev.server.web.util.DeleteCallback;

@SuppressWarnings("serial")
class PullRequestCommentedPanel extends GenericPanel<PullRequestComment> {

	private final DeleteCallback deleteCallback;
	
	public PullRequestCommentedPanel(String id, IModel<PullRequestComment> model, DeleteCallback deleteCallback) {
		super(id, model);
		this.deleteCallback = deleteCallback;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("user", getComment().getUser().getDisplayName()));
		add(new Label("age", DateUtils.formatAge(getComment().getDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(getComment().getDate()))));
		
		add(new SinceChangesLink("changes", new AbstractReadOnlyModel<PullRequest>() {

			@Override
			public PullRequest getObject() {
				return getComment().getRequest();
			}

		}, getComment().getDate()));
		
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
				return PullRequestCommentedPanel.this.getComment().getContent();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				if (comment.length() > PullRequestComment.MAX_CONTENT_LEN)
					throw new ExplicitException("Comment too long");
				PullRequestCommentedPanel.this.getComment().setContent(comment);
				OneDev.getInstance(PullRequestCommentManager.class).save(PullRequestCommentedPanel.this.getComment());
			}

			@Override
			protected Project getProject() {
				return PullRequestCommentedPanel.this.getComment().getProject();
			}

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), 
						PullRequestCommentedPanel.this.getComment().getRequest().getUUID(), 
						SecurityUtils.canManagePullRequests(getProject()));
			}

			@Override
			protected List<User> getMentionables() {
				UserCache cache = OneDev.getInstance(UserManager.class).cloneCache();		
				List<User> users = new ArrayList<>(cache.getUsers());
				users.sort(cache.comparingDisplayName(PullRequestCommentedPanel.this.getComment().getRequest().getParticipants()));
				return users;
			}
			
			@Override
			protected boolean canModifyOrDeleteComment() {
				return SecurityUtils.canModifyOrDelete(PullRequestCommentedPanel.this.getComment());
			}

			@Override
			protected String getRequiredLabel() {
				return "Comment";
			}

			@Override
			protected ContentVersionSupport getContentVersionSupport() {
				return new ContentVersionSupport() {

					@Override
					public long getVersion() {
						return 0;
					}

				};
			}

			@Override
			protected DeleteCallback getDeleteCallback() {
				return deleteCallback;
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	private PullRequestComment getComment() {
		return getModelObject();
	}
	
}
