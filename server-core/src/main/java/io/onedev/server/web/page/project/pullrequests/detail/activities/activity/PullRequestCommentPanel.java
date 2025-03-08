package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.jetbrains.annotations.Nullable;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.entitymanager.PullRequestCommentReactionManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;
import io.onedev.server.model.support.EntityReaction;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.comment.CommentPanel;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.SinceChangesLink;
import io.onedev.server.web.util.DeleteCallback;

class PullRequestCommentPanel extends Panel {
	
	public PullRequestCommentPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new UserIdentPanel("avatar", getComment().getUser(), Mode.AVATAR));
		add(new Label("name", getComment().getUser().getDisplayName()));
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
				return PullRequestCommentPanel.this.getComment().getContent();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				if (comment.length() > PullRequestComment.MAX_CONTENT_LEN)
					throw new ExplicitException("Comment too long");
				var entity = PullRequestCommentPanel.this.getComment();
				entity.setContent(comment);
				getPullRequestCommentManager().update(entity);
				var page = (BasePage) getPage();
				page.notifyObservableChange(target, PullRequest.getChangeObservable(entity.getRequest().getId()));				
			}

			@Override
			protected Project getProject() {
				return PullRequestCommentPanel.this.getComment().getProject();
			}

			@Nullable
			@Override
			protected String getAutosaveKey() {
				return "pull-request-comment:" + PullRequestCommentPanel.this.getComment().getId();
			}

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), 
						PullRequestCommentPanel.this.getComment().getRequest().getUUID(), 
						SecurityUtils.canManagePullRequests(getProject()));
			}

			@Override
			protected List<User> getParticipants() {
				return PullRequestCommentPanel.this.getComment().getRequest().getParticipants();
			}
			
			@Override
			protected boolean canManageComment() {
				return SecurityUtils.canModifyOrDelete(PullRequestCommentPanel.this.getComment());
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
					var page = (BasePage) getPage();
					var pullRequest = PullRequestCommentPanel.this.getComment().getRequest();
					target.appendJavaScript(String.format("$('#%s').remove();", PullRequestCommentPanel.this.getMarkupId()));
					PullRequestCommentPanel.this.remove();
					getPullRequestCommentManager().delete(PullRequestCommentPanel.this.getComment());
					page.notifyObservableChange(target, PullRequest.getChangeObservable(pullRequest.getId()));
				};
			}

			@Override
			protected Collection<? extends EntityReaction> getReactions() {
				return PullRequestCommentPanel.this.getComment().getReactions();
			}

			@Override
			protected void onToggleEmoji(AjaxRequestTarget target, String emoji) {
				OneDev.getInstance(PullRequestCommentReactionManager.class).toggleEmoji(
						SecurityUtils.getUser(), 
						PullRequestCommentPanel.this.getComment(), 
						emoji);
			}
			
		});

		setMarkupId(getComment().getAnchor());
		setOutputMarkupId(true);
	}

	private PullRequestCommentManager getPullRequestCommentManager() {
		return OneDev.getInstance(PullRequestCommentManager.class);
	}

	private PullRequestComment getComment() {
		return ((PullRequestCommentActivity) getDefaultModelObject()).getComment();
	}

}
