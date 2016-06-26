package com.pmease.gitplex.web.component.comment;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.hibernate.StaleObjectStateException;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.ajaxlistener.ConfirmListener;
import com.pmease.commons.wicket.behavior.markdown.AttachmentSupport;
import com.pmease.commons.wicket.component.markdownviewer.MarkdownViewer;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.component.CompareContext;
import com.pmease.gitplex.core.entity.component.DepotAndRevision;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.CodeCommentReplyManager;
import com.pmease.gitplex.core.manager.VisitInfoManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;
import com.pmease.gitplex.web.util.DateUtils;
import com.pmease.gitplex.web.websocket.CodeCommentChangeRenderer;
import com.pmease.gitplex.web.websocket.CodeCommentChanged;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public abstract class CodeCommentPanel extends Panel {

	private final Long commentId;
	
	private RepeatingView repliesView;
	
	/**
	 * We pass comment id instead of comment model as we want to make sure that 
	 * comment is always loaded upon usage as this panel is involved in websocket
	 * update and websocket update does not detach models at end of request which 
	 * may result in Hibernate lazy load exception when the comment is used again
	 * in next request 
	 * 
	 * @param id
	 * @param commentId
	 */
	public CodeCommentPanel(String id, Long commentId) {
		super(id);
		this.commentId = commentId;
	}

	protected CodeComment getComment() {
		return GitPlex.getInstance(CodeCommentManager.class).load(commentId);
	}
	
	@Override
	protected void onAfterRender() {
		super.onAfterRender();
		Account user = SecurityUtils.getAccount();
		if (user != null) 
			GitPlex.getInstance(VisitInfoManager.class).visit(user, getComment());
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof CodeCommentChanged) {
			CodeCommentChanged codeCommentChanged = (CodeCommentChanged) event.getPayload();
			IPartialPageRequestHandler partialPageRequestHandler = codeCommentChanged.getPartialPageRequestHandler();

			Date lastPostDate;
			String prevPostMarkupId;
			if (repliesView.size() != 0) {
				@SuppressWarnings("deprecation")
				Component lastReplyContainer = repliesView.get(repliesView.size()-1);
				CodeCommentReply lastReply = GitPlex.getInstance(CodeCommentReplyManager.class)
						.load((Long) lastReplyContainer.getDefaultModelObject());
				lastPostDate = lastReply.getDate();
				prevPostMarkupId = lastReplyContainer.getMarkupId();
			} else {
				lastPostDate = getComment().getCreateDate();
				prevPostMarkupId = get("comment").getMarkupId();
			}
			for (CodeCommentReply reply: getComment().getSortedReplies()) {
				if (reply.getDate().after(lastPostDate)) {
					Component newReplyContainer = newReplyContainer(repliesView.newChildId(), reply.getId()); 
					repliesView.add(newReplyContainer);
					
					String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
							newReplyContainer.getMarkupId(), prevPostMarkupId);
					partialPageRequestHandler.prependJavaScript(script);
					partialPageRequestHandler.add(newReplyContainer);
					prevPostMarkupId = newReplyContainer.getMarkupId();
				}
			}
		}
	}

	private WebMarkupContainer newCommentContainer() {
		WebMarkupContainer commentContainer = new Fragment("comment", "viewFrag", this);
		commentContainer.setOutputMarkupId(true);
		
		commentContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getComment().isVisited(false)?"": "new";
			}
			
		}));
		
		commentContainer.add(new AvatarLink("authorAvatar", getComment().getUser()));
		commentContainer.add(new AccountLink("authorName", getComment().getUser()));
		commentContainer.add(new Label("authorDate", DateUtils.formatAge(getComment().getCreateDate())));
		commentContainer.add(new Link<Void>("compareContext") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getCompareContext(getComment()).equals(getComment().getCompareContext()));
			}

			@Override
			public void onClick() {
				CodeComment comment = getComment();
				CompareContext compareContext = comment.getCompareContext();
				PullRequest request = getPullRequest();
				if (request != null) {
					RequestChangesPage.State state = new RequestChangesPage.State();
					state.commentId = comment.getId();
					if (compareContext.isLeftSide()) {
						state.oldCommit = compareContext.getCompareCommit();
						state.newCommit = comment.getCommentPos().getCommit();
					} else {
						state.oldCommit = comment.getCommentPos().getCommit();
						state.newCommit = compareContext.getCompareCommit();
					}
					state.mark = comment.getCommentPos();
					state.pathFilter = compareContext.getPathFilter();
					state.whitespaceOption = compareContext.getWhitespaceOption();
					PageParameters params  = RequestChangesPage.paramsOf(request, state);
					setResponsePage(RequestChangesPage.class, params);
				} else {
					RevisionComparePage.State state = new RevisionComparePage.State();
					state.commentId = comment.getId();
					state.compareWithMergeBase = false;
					if (compareContext.isLeftSide()) {
						state.leftSide = new DepotAndRevision(comment.getDepot(), compareContext.getCompareCommit());
						state.rightSide = new DepotAndRevision(comment.getDepot(), comment.getCommentPos().getCommit());
					} else {
						state.leftSide = new DepotAndRevision(comment.getDepot(), comment.getCommentPos().getCommit());
						state.rightSide = new DepotAndRevision(comment.getDepot(), compareContext.getCompareCommit());
					}
					state.mark = comment.getCommentPos();
					state.pathFilter = compareContext.getPathFilter();
					state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
					state.whitespaceOption = compareContext.getWhitespaceOption();
					PageParameters params  = RevisionComparePage.paramsOf(comment.getDepot(), state);
					setResponsePage(RevisionComparePage.class, params);
				}
			}
			
		}.add(AttributeAppender.append("title", "This comment is added in a different compare context, click to show")));

		NotificationPanel feedback = new NotificationPanel("feedback", commentContainer);
		feedback.setOutputMarkupPlaceholderTag(true);
		commentContainer.add(feedback);
		AtomicLong lastVersionRef = new AtomicLong(getComment().getVersion());
		commentContainer.add(new MarkdownViewer("content", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return getComment().getContent();
			}

			@Override
			public void setObject(String object) {
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				Preconditions.checkNotNull(target);
				CodeComment comment = getComment();
				try {
					if (comment.getVersion() != lastVersionRef.get())
						throw new StaleObjectStateException(CodeComment.class.getName(), comment.getId());
					comment.setContent(object);
					GitPlex.getInstance(CodeCommentManager.class).save(comment);				
					target.add(feedback); // clear the feedback
				} catch (StaleObjectStateException e) {
					commentContainer.warn("Some one changed the content you are editing. "
							+ "The content has now been reloaded, please try again.");
					target.add(commentContainer);
				}
				lastVersionRef.set(comment.getVersion());
			}
			
		}, SecurityUtils.canModify(getComment())));

		WebMarkupContainer foot = new WebMarkupContainer("foot");
		foot.setVisible(SecurityUtils.canModify(getComment()));
		foot.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment(commentContainer.getId(), "commentEditFrag", CodeCommentPanel.this);
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				
				TextField<String> titleInput = new TextField<String>("title", Model.of(getComment().getTitle()));
				titleInput.setRequired(true);
				form.add(titleInput);
				CommentInput contentInput = new CommentInput("content", Model.of(getComment().getContent())) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new DepotAttachmentSupport(getComment().getDepot(), getComment().getUUID());
					}

					@Override
					protected Depot getDepot() {
						return getComment().getDepot();
					}
					
				};
				form.add(contentInput);
				contentInput.setRequired(true);
				
				NotificationPanel feedback = new NotificationPanel("feedback", form); 
				feedback.setOutputMarkupPlaceholderTag(true);
				form.add(feedback);
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(form));
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						WebMarkupContainer commentContainer = newCommentContainer();
						fragment.replaceWith(commentContainer);
						target.add(commentContainer);
					}
					
				});
				
				long lastVersion = getComment().getVersion();
				form.add(new AjaxButton("save") {

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(feedback);
					}

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);

						try {
							CodeComment comment = getComment();
							if (comment.getVersion() != lastVersion)
								throw new StaleObjectStateException(CodeComment.class.getName(), comment.getId());
							comment.setTitle(titleInput.getModelObject());
							comment.setContent(contentInput.getModelObject());
							GitPlex.getInstance(CodeCommentManager.class).save(comment);
							WebMarkupContainer commentContainer = newCommentContainer();
							fragment.replaceWith(commentContainer);
							target.add(commentContainer);
							onSaveComment(target, comment);
						} catch (StaleObjectStateException e) {
							error("Some one changed the content you are editing. Reload the page and try again.");
							target.add(feedback);
						}
					}

				});
				
				fragment.add(form);
				fragment.setOutputMarkupId(true);
				commentContainer.replaceWith(fragment);
				target.add(fragment);
			}
			
		});
		
		foot.add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				String confirmMessage;
				if (getComment().getReplies().isEmpty()) {
					confirmMessage = "Do you really want to delete this comment?";
				} else {
					confirmMessage = "Deleting this comment will also delete all replies, do you really "
							+ "want to continue?";
				}
				attributes.getAjaxCallListeners().add(new ConfirmListener(confirmMessage));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDeleteComment(target, getComment());
				GitPlex.getInstance(CodeCommentManager.class).delete(getComment());
			}
			
		});
		
		commentContainer.add(foot);		
		return commentContainer;
	}
	
	private WebMarkupContainer newReplyContainer(String componentId, Long replyId) {
		Fragment replyContainer = new Fragment(componentId, "viewFrag", this, Model.of(replyId));
		replyContainer.setOutputMarkupId(true);
		
		replyContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getReply(replyId).isVisited()?"":"new";
			}
			
		}));
		
		replyContainer.add(new AvatarLink("authorAvatar", getReply(replyId).getUser()));
		replyContainer.add(new AccountLink("authorName", getReply(replyId).getUser()));
		replyContainer.add(new Label("authorDate", DateUtils.formatAge(getReply(replyId).getDate())));
		replyContainer.add(new Link<Void>("compareContext") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getCompareContext(getComment()).equals(getReply(replyId).getCompareContext()));
			}

			@Override
			public void onClick() {
				CodeCommentReply reply = getReply(replyId);
				CompareContext compareContext = reply.getCompareContext();
				PullRequest request = getPullRequest();
				if (request != null) {
					RequestChangesPage.State state = new RequestChangesPage.State();
					state.commentId = reply.getComment().getId();
					if (compareContext.isLeftSide()) {
						state.oldCommit = compareContext.getCompareCommit();
						state.newCommit = reply.getComment().getCommentPos().getCommit();
					} else {
						state.oldCommit = reply.getComment().getCommentPos().getCommit();
						state.newCommit = compareContext.getCompareCommit();
					}
					state.mark = reply.getComment().getCommentPos();
					state.pathFilter = compareContext.getPathFilter();
					state.whitespaceOption = compareContext.getWhitespaceOption();
					PageParameters params  = RequestChangesPage.paramsOf(request, state);
					setResponsePage(RequestChangesPage.class, params);
				} else {
					RevisionComparePage.State state = new RevisionComparePage.State();
					CodeComment comment = reply.getComment();
					state.commentId = comment.getId();
					state.compareWithMergeBase = false;
					if (compareContext.isLeftSide()) {
						state.leftSide = new DepotAndRevision(comment.getDepot(), compareContext.getCompareCommit());
						state.rightSide = new DepotAndRevision(comment.getDepot(), comment.getCommentPos().getCommit());
					} else {
						state.leftSide = new DepotAndRevision(comment.getDepot(), comment.getCommentPos().getCommit());
						state.rightSide = new DepotAndRevision(comment.getDepot(), compareContext.getCompareCommit());
					}
					state.mark = comment.getCommentPos();
					state.pathFilter = compareContext.getPathFilter();
					state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
					state.whitespaceOption = compareContext.getWhitespaceOption();
					PageParameters params  = RevisionComparePage.paramsOf(comment.getDepot(), state);
					setResponsePage(RevisionComparePage.class, params);
				}
			}
			
		}.add(AttributeAppender.append("title", "This reply is added in a different compare context, click to show")));		

		NotificationPanel feedback = new NotificationPanel("feedback", replyContainer);
		feedback.setOutputMarkupPlaceholderTag(true);
		replyContainer.add(feedback);
		AtomicLong lastVersionRef = new AtomicLong(getReply(replyId).getVersion());
		replyContainer.add(new MarkdownViewer("content", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return getReply(replyId).getContent();
			}

			@Override
			public void setObject(String object) {
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				Preconditions.checkNotNull(target);
				CodeCommentReply reply = getReply(replyId);
				try {
					if (reply.getVersion() != lastVersionRef.get())
						throw new StaleObjectStateException(CodeCommentReply.class.getName(), reply.getId());
					reply.setContent(object);
					GitPlex.getInstance(CodeCommentReplyManager.class).save(reply);				
					target.add(feedback); // clear the feedback
				} catch (StaleObjectStateException e) {
					replyContainer.warn("Some one changed the content you are editing. The content has now been reloaded, "
							+ "please try again.");
					target.add(replyContainer);
				}
				lastVersionRef.set(reply.getVersion());
			}
			
		}, SecurityUtils.canModify(getComment())));
		
		WebMarkupContainer foot = new WebMarkupContainer("foot");
		foot.setVisible(SecurityUtils.canModify(getReply(replyId)));
		
		foot.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment(replyContainer.getId(), "replyEditFrag", 
						CodeCommentPanel.this, Model.of(replyId));
				Form<?> form = new Form<Void>("form");
				CommentInput contentInput = new CommentInput("content", Model.of(getReply(replyId).getContent())) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new DepotAttachmentSupport(getComment().getDepot(), getComment().getUUID());
					}

					@Override
					protected Depot getDepot() {
						return getComment().getDepot();
					}
					
				};
				form.add(contentInput);
				contentInput.setRequired(true);
				
				NotificationPanel feedback = new NotificationPanel("feedback", form); 
				feedback.setOutputMarkupPlaceholderTag(true);
				form.add(feedback);
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(form));
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						WebMarkupContainer replyContainer = newReplyContainer(componentId, replyId);
						fragment.replaceWith(replyContainer);
						target.add(replyContainer);
					}
					
				});
				
				long lastVersion = getReply(replyId).getVersion();
				form.add(new AjaxButton("save") {

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(feedback);
					}

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);

						try {
							CodeCommentReply reply = getReply(replyId);
							if (reply.getVersion() != lastVersion)
								throw new StaleObjectStateException(CodeComment.class.getName(), reply.getId());
							reply.setContent(contentInput.getModelObject());
							GitPlex.getInstance(CodeCommentReplyManager.class).save(reply);
							WebMarkupContainer replyContainer = newReplyContainer(componentId, replyId);
							fragment.replaceWith(replyContainer);
							target.add(replyContainer);
						} catch (StaleObjectStateException e) {
							error("Some one changed the content you are editing. Reload the page and try again.");
							target.add(feedback);
						}
					}

				}.add(new Label("label", "Save")));
				
				fragment.add(form);
				fragment.setOutputMarkupId(true);
				replyContainer.replaceWith(fragment);
				target.add(fragment);
			}
			
		});
		foot.add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this reply?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				replyContainer.remove();
				GitPlex.getInstance(CodeCommentReplyManager.class).delete(getReply(replyId));
				String script = String.format("$('#%s').remove();", replyContainer.getMarkupId());
				target.appendJavaScript(script);
			}
			
		});
		
		replyContainer.add(foot);		
		return replyContainer;
	}
	
	private WebMarkupContainer newAddReplyContainer() {
		WebMarkupContainer addReplyContainer = new Fragment("addReply", "addReplyFrag", this) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getAccount() != null);
			}
			
		};
		addReplyContainer.setOutputMarkupId(true);
		addReplyContainer.add(new AjaxLink<Void>("reply") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onAddReply(target, false);
			}
			
		});
		return addReplyContainer;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newCommentContainer());
		
		repliesView = new RepeatingView("replies");
		PullRequest request = getPullRequest();
		for (CodeCommentReply reply: getComment().getSortedReplies()) {
			if (request == null || request.getRequestComparingInfo(reply.getComparingInfo()) != null) {
				repliesView.add(newReplyContainer(repliesView.newChildId(), reply.getId()));
			}
		}
		add(repliesView);
		add(newAddReplyContainer());
		
		setOutputMarkupId(true);
		
		add(new CodeCommentChangeRenderer(getComment().getId()));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				CodeCommentPanel.class, "code-comment.css")));
	}

	private CodeCommentReply getReply(Long replyId) {
		return GitPlex.getInstance(CodeCommentReplyManager.class).load(replyId);
	}
	
	private void onAddReply(AjaxRequestTarget target, boolean toggleResolve) {
		Fragment fragment = new Fragment("addReply", "replyEditFrag", CodeCommentPanel.this);
		Form<?> form = new Form<Void>("form");
		CommentInput contentInput = new CommentInput("content", Model.of("")) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new DepotAttachmentSupport(getComment().getDepot(), getComment().getUUID());
			}

			@Override
			protected Depot getDepot() {
				return getComment().getDepot();
			}
			
		};
		contentInput.setRequired(!toggleResolve);
		form.add(contentInput);
		
		NotificationPanel feedback = new NotificationPanel("feedback", form); 
		feedback.setOutputMarkupPlaceholderTag(true);
		form.add(feedback);
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(form));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				WebMarkupContainer addReplyContainer = newAddReplyContainer();
				fragment.replaceWith(addReplyContainer);
				target.add(addReplyContainer);
			}
			
		});
		
		AjaxButton saveButton = new AjaxButton("save") {

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(feedback);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				if (toggleResolve) {
					String replyContent = contentInput.getModelObject();
					CodeCommentManager manager = GitPlex.getInstance(CodeCommentManager.class);				
					if (replyContent != null) {
						CodeCommentReply reply = newReply(replyContent);
						getComment().setResolved(!getComment().isResolved());
						manager.save(getComment(), reply);
						onReplyAdded(target, fragment, reply);
					} else {
						getComment().setResolved(!getComment().isResolved());
						manager.save(getComment());
						onReplyAdded(target, fragment, null);
					}
					onSaveComment(target, getComment());
				} else {
					CodeCommentReply reply = newReply(contentInput.getModelObject());
					GitPlex.getInstance(CodeCommentReplyManager.class).save(reply);
					onReplyAdded(target, fragment, reply);
				}
			}

		};
		if (toggleResolve) {
			saveButton.add(new Label("label", getComment().isResolved()?"Confirm unresolve":"Confirm resolve"));
		} else {
			saveButton.add(new Label("label", "Save"));
			saveButton.add(AttributeAppender.append("class", "dirty-aware"));
		}
		form.add(saveButton);
		
		fragment.add(form);
		fragment.setOutputMarkupId(true);
		get("addReply").replaceWith(fragment);
		target.add(fragment);				
	}
	
	public void onToggleResolve(AjaxRequestTarget target) {
		onAddReply(target, true);
	}
	
	private void onReplyAdded(AjaxRequestTarget target, Fragment fragment, @Nullable CodeCommentReply reply) {
		if (reply != null) {
			WebMarkupContainer replyContainer = newReplyContainer(repliesView.newChildId(), reply.getId());
			repliesView.add(replyContainer);
	
			String script = String.format("$('#%s .add-reply').before('<div id=\"%s\"></div>');", 
					CodeCommentPanel.this.getMarkupId(), replyContainer.getMarkupId());
			target.prependJavaScript(script);
			target.add(replyContainer);
		}
		
		WebMarkupContainer addReplyContainer = newAddReplyContainer();
		fragment.replaceWith(addReplyContainer);
		target.add(addReplyContainer);
	}
	
	private CodeCommentReply newReply(String content) {
		CodeCommentReply reply = new CodeCommentReply();
		reply.setComment(getComment());
		reply.setUser(SecurityUtils.getAccount());
		reply.setContent(content);
		reply.setCompareContext(getCompareContext(getComment()));
		return reply;
	}
	
	protected abstract void onDeleteComment(AjaxRequestTarget target, CodeComment comment);
	
	protected abstract void onSaveComment(AjaxRequestTarget target, CodeComment comment);
	
	protected abstract CompareContext getCompareContext(CodeComment comment);
	
	@Nullable
	protected abstract PullRequest getPullRequest();
}
