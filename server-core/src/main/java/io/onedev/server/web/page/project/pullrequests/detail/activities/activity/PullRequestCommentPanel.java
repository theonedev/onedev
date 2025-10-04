package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import static io.onedev.server.web.translation.Translation._T;

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
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.jetbrains.annotations.Nullable;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.service.PullRequestCommentReactionService;
import io.onedev.server.service.PullRequestCommentRevisionService;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestCommentRevision;
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
		add(new UserIdentPanel("name", getComment().getUser(), Mode.NAME));
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

				var oldComment = entity.getContent();
				if (!oldComment.equals(comment)) {
					getTransactionService().run(() -> {
						entity.setContent(comment);
						entity.setRevisionCount(entity.getRevisionCount() + 1);
						getPullRequestCommentService().update(entity);

						var revision = new PullRequestCommentRevision();
						revision.setComment(entity);
						revision.setUser(SecurityUtils.getUser());
						revision.setOldContent(oldComment);
						revision.setNewContent(comment);
						getPullRequestCommentRevisionService().create(revision);
					});
					var page = (BasePage) getPage();
					page.notifyObservableChange(target, PullRequest.getChangeObservable(entity.getRequest().getId()));				
				}
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
				return _T("Comment");
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
					getPullRequestCommentService().delete(PullRequestCommentPanel.this.getComment());
					page.notifyObservableChange(target, PullRequest.getChangeObservable(pullRequest.getId()));
				};
			}

			@Override
			protected ReactionSupport getReactionSupport() {
				return new ReactionSupport() {

					@Override
					public Collection<? extends EntityReaction> getReactions() {
						return PullRequestCommentPanel.this.getComment().getReactions();
					}
		
					@Override
					public void onToggleEmoji(AjaxRequestTarget target, String emoji) {
						getPullRequestCommentReactionService().toggleEmoji(
								SecurityUtils.getUser(), 
								PullRequestCommentPanel.this.getComment(), 
								emoji);
					}
							
				};
			}
			
			@Override
			protected Component newMoreActions(String id) {
				var fragment = new Fragment(id, "historyFrag", PullRequestCommentPanel.this);
				fragment.add(new CommentHistoryLink("history") {

					@Override
					protected Collection<? extends CommentRevision> getCommentRevisions() {
						return PullRequestCommentPanel.this.getComment().getRevisions();
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(PullRequestCommentPanel.this.getComment().getRevisionCount() != 0);
					}

				});
				return fragment;
			}

		});

		setMarkupId(getComment().getAnchor());
		setOutputMarkupId(true);
	}

	private TransactionService getTransactionService() {
		return OneDev.getInstance(TransactionService.class);
	}

	private PullRequestCommentRevisionService getPullRequestCommentRevisionService() {
		return OneDev.getInstance(PullRequestCommentRevisionService.class);
	}

	private PullRequestCommentService getPullRequestCommentService() {
		return OneDev.getInstance(PullRequestCommentService.class);
	}

	private PullRequestCommentReactionService getPullRequestCommentReactionService() {
		return OneDev.getInstance(PullRequestCommentReactionService.class);
	}

	private PullRequestComment getComment() {
		return ((PullRequestCommentActivity) getDefaultModelObject()).getComment();
	}

}
