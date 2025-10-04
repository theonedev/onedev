package io.onedev.server.web.component.issue.activities.activity;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.jetbrains.annotations.Nullable;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.service.IssueCommentService;
import io.onedev.server.service.IssueCommentReactionService;
import io.onedev.server.service.IssueCommentRevisionService;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueCommentRevision;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CommentRevision;
import io.onedev.server.model.support.EntityReaction;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.comment.CommentHistoryLink;
import io.onedev.server.web.component.comment.CommentPanel;
import io.onedev.server.web.component.comment.ReactionSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.DeleteCallback;

class IssueCommentPanel extends Panel {

	public IssueCommentPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new UserIdentPanel("avatar", getComment().getUser(), Mode.AVATAR));
		add(new UserIdentPanel("name", getComment().getUser(), Mode.NAME));
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
				return IssueCommentPanel.this.getComment().getContent();
			}

			@Override
			protected boolean isQuoteEnabled() {
				return true;
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				if (comment.length() > IssueComment.MAX_CONTENT_LEN)
					throw new ExplicitException("Comment too long");
				var entity = IssueCommentPanel.this.getComment();
				var oldComment = entity.getContent();
				if (!oldComment.equals(comment)) {
					getTransactionService().run(() -> {
						entity.setContent(comment);
						entity.setRevisionCount(entity.getRevisionCount() + 1);
						getIssueCommentService().update(entity);

						var revision = new IssueCommentRevision();
						revision.setComment(entity);
						revision.setUser(SecurityUtils.getUser());
						revision.setOldContent(oldComment);
						revision.setNewContent(comment);
						getIssueCommentRevisionManager().create(revision);
					});
					var page = (BasePage) getPage();
					page.notifyObservablesChange(target, entity.getIssue().getChangeObservables(false));				
				}
			}

			@Override
			protected Project getProject() {
				return IssueCommentPanel.this.getComment().getIssue().getProject();
			}

			@Nullable
			@Override
			protected String getAutosaveKey() {
				return "issue-comment:" + IssueCommentPanel.this.getComment().getId();
			}

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), 
						IssueCommentPanel.this.getComment().getIssue().getUUID(), 
						SecurityUtils.canManageIssues(getProject()));
			}

			@Override
			protected List<User> getParticipants() {
				return IssueCommentPanel.this.getComment().getIssue().getParticipants();
			}
			
			@Override
			protected boolean canManageComment() {
				return SecurityUtils.canModifyOrDelete(IssueCommentPanel.this.getComment());
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
					var issue = IssueCommentPanel.this.getComment().getIssue();
					target.appendJavaScript(String.format("$('#%s').remove();", IssueCommentPanel.this.getMarkupId()));	
					IssueCommentPanel.this.remove();
					getIssueCommentService().delete(IssueCommentPanel.this.getComment());
					page.notifyObservablesChange(target, issue.getChangeObservables(false));					
				};
			}

			@Override
			protected ReactionSupport getReactionSupport() {
				return new ReactionSupport() {

					@Override
					public Collection<? extends EntityReaction> getReactions() {
						return IssueCommentPanel.this.getComment().getReactions();
					}
		
					@Override
					public void onToggleEmoji(AjaxRequestTarget target, String emoji) {
						getIssueCommentReactionManager().toggleEmoji(
								SecurityUtils.getUser(), 
								IssueCommentPanel.this.getComment(), 
								emoji);
					}
					
				};
			}

			@Override
			protected Component newMoreActions(String id) {
				var fragment = new Fragment(id, "historyFrag", IssueCommentPanel.this);
				fragment.add(new CommentHistoryLink("history") {

					@Override
					protected Collection<? extends CommentRevision> getCommentRevisions() {
						return IssueCommentPanel.this.getComment().getRevisions();
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(IssueCommentPanel.this.getComment().getRevisionCount() != 0);
					}

				});
				return fragment;
			}
		});

		setMarkupId(getComment().getAnchor());
		setOutputMarkupId(true);
	}
	
	private IssueComment getComment() {
		return ((IssueCommentActivity) getDefaultModelObject()).getComment();
	}
	
	private TransactionService getTransactionService() {
		return OneDev.getInstance(TransactionService.class);
	}

	private IssueCommentRevisionService getIssueCommentRevisionManager() {
		return OneDev.getInstance(IssueCommentRevisionService.class);
	}

	private IssueCommentService getIssueCommentService() {
		return OneDev.getInstance(IssueCommentService.class);
	}

	private IssueCommentReactionService getIssueCommentReactionManager() {
		return OneDev.getInstance(IssueCommentReactionService.class);
	}
}
