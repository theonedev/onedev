package com.pmease.gitplex.web.component.comment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
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
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.component.CompareContext;
import com.pmease.gitplex.core.entity.component.DepotAndRevision;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.CodeCommentReplyManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.component.diff.revision.DiffMark;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public abstract class CodeCommentPanel extends GenericPanel<CodeComment> {

	private RepeatingView repliesView;
	
	public CodeCommentPanel(String id, IModel<CodeComment> commentModel) {
		super(id, commentModel);
	}

	private WebMarkupContainer newCommentContainer() {
		WebMarkupContainer commentContainer = new Fragment("comment", "viewFrag", this);
		commentContainer.setOutputMarkupId(true);
		
		commentContainer.add(new AvatarLink("authorAvatar", getComment().getUser()));
		commentContainer.add(new AccountLink("authorName", getComment().getUser()));
		commentContainer.add(new Label("authorDate", DateUtils.formatAge(getComment().getCreateDate())));
		commentContainer.add(new Link<Void>("compareContext") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getCompareContext().equals(getComment().getCompareContext()));
			}

			@Override
			public void onClick() {
				CodeComment comment = getComment();
				CompareContext compareContext = comment.getCompareContext();
				RevisionComparePage.State state = new RevisionComparePage.State();
				state.commentId = comment.getId();
				state.compareWithMergeBase = false;
				if (compareContext.isLeftSide()) {
					state.leftSide = new DepotAndRevision(comment.getDepot(), compareContext.getCompareCommit());
					state.rightSide = new DepotAndRevision(comment.getDepot(), comment.getCommit());
				} else {
					state.leftSide = new DepotAndRevision(comment.getDepot(), comment.getCommit());
					state.rightSide = new DepotAndRevision(comment.getDepot(), compareContext.getCompareCommit());
				}
				state.mark = new DiffMark(comment);
				state.pathFilter = compareContext.getPathFilter();
				state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
				state.whitespaceOption = compareContext.getWhitespaceOption();
				PageParameters params  = RevisionComparePage.paramsOf(comment.getDepot(), state);
				setResponsePage(RevisionComparePage.class, params);
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
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canModify(getComment()));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment(commentContainer.getId(), "editFrag", CodeCommentPanel.this);
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				CommentInput input = new CommentInput("input", Model.of(getComment().getContent())) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new DepotAttachmentSupport(getComment().getDepot(), getComment().getUUID());
					}

					@Override
					protected Depot getDepot() {
						return getComment().getDepot();
					}
					
				};
				form.add(input);
				input.setRequired(true);
				
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
							comment.setContent(input.getModelObject());
							GitPlex.getInstance(CodeCommentManager.class).save(comment);
							WebMarkupContainer commentContainer = newCommentContainer();
							fragment.replaceWith(commentContainer);
							target.add(commentContainer);
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
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canModify(getComment()));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				GitPlex.getInstance(CodeCommentManager.class).delete(getComment());
				onCommentDeleted(target);
			}
			
		});
		
		commentContainer.add(foot);		
		return commentContainer;
	}
	
	private WebMarkupContainer newReplyContainer(String componentId, Long replyId) {
		Fragment replyContainer = new Fragment(componentId, "viewFrag", this);
		replyContainer.setOutputMarkupId(true);
		
		replyContainer.add(new AvatarLink("authorAvatar", getReply(replyId).getUser()));
		replyContainer.add(new AccountLink("authorName", getReply(replyId).getUser()));
		replyContainer.add(new Label("authorDate", DateUtils.formatAge(getReply(replyId).getDate())));
		replyContainer.add(new Link<Void>("compareContext") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getCompareContext().equals(getReply(replyId).getCompareContext()));
			}

			@Override
			public void onClick() {
				CodeCommentReply reply = getReply(replyId);
				CompareContext compareContext = reply.getCompareContext();
				RevisionComparePage.State state = new RevisionComparePage.State();
				CodeComment comment = reply.getComment();
				state.commentId = comment.getId();
				state.compareWithMergeBase = false;
				if (compareContext.isLeftSide()) {
					state.leftSide = new DepotAndRevision(comment.getDepot(), compareContext.getCompareCommit());
					state.rightSide = new DepotAndRevision(comment.getDepot(), comment.getCommit());
				} else {
					state.leftSide = new DepotAndRevision(comment.getDepot(), comment.getCommit());
					state.rightSide = new DepotAndRevision(comment.getDepot(), compareContext.getCompareCommit());
				}
				state.mark = new DiffMark(comment);
				state.pathFilter = compareContext.getPathFilter();
				state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
				state.whitespaceOption = compareContext.getWhitespaceOption();
				PageParameters params  = RevisionComparePage.paramsOf(comment.getDepot(), state);
				setResponsePage(RevisionComparePage.class, params);
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
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canModify(getReply(replyId)));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment(replyContainer.getId(), "editFrag", CodeCommentPanel.this);
				Form<?> form = new Form<Void>("form");
				CommentInput input = new CommentInput("input", Model.of(getReply(replyId).getContent())) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new DepotAttachmentSupport(getComment().getDepot(), getComment().getUUID());
					}

					@Override
					protected Depot getDepot() {
						return getComment().getDepot();
					}
					
				};
				form.add(input);
				input.setRequired(true);
				
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
							reply.setContent(input.getModelObject());
							GitPlex.getInstance(CodeCommentReplyManager.class).save(reply);
							WebMarkupContainer replyContainer = newReplyContainer(componentId, replyId);
							fragment.replaceWith(replyContainer);
							target.add(replyContainer);
						} catch (StaleObjectStateException e) {
							error("Some one changed the content you are editing. Reload the page and try again.");
							target.add(feedback);
						}
					}

				});
				
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
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canModify(getComment()));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
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
				Fragment fragment = new Fragment(addReplyContainer.getId(), "editFrag", CodeCommentPanel.this);
				Form<?> form = new Form<Void>("form");
				CommentInput input = new CommentInput("input", Model.of("")) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new DepotAttachmentSupport(getComment().getDepot(), getComment().getUUID());
					}

					@Override
					protected Depot getDepot() {
						return getComment().getDepot();
					}
					
				};
				form.add(input);
				input.setRequired(true);
				
				NotificationPanel feedback = new NotificationPanel("feedback", input); 
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
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(feedback);
					}

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);

						CodeCommentReply reply = new CodeCommentReply();
						reply.setComment(getComment());
						reply.setUser(SecurityUtils.getAccount());
						reply.setContent(input.getModelObject());
						reply.setCompareContext(getCompareContext());
						GitPlex.getInstance(CodeCommentReplyManager.class).save(reply);
						
						WebMarkupContainer replyContainer = newReplyContainer(repliesView.newChildId(), reply.getId());
						repliesView.add(replyContainer);

						String script = String.format("$('#%s .add-reply').before('<div id=\"%s\"></div>');", 
								CodeCommentPanel.this.getMarkupId(), replyContainer.getMarkupId());
						target.prependJavaScript(script);
						target.add(replyContainer);
						
						WebMarkupContainer addReplyContainer = newAddReplyContainer();
						fragment.replaceWith(addReplyContainer);
						target.add(addReplyContainer);
					}

				});
				
				fragment.add(form);
				fragment.setOutputMarkupId(true);
				addReplyContainer.replaceWith(fragment);
				target.add(fragment);				
			}
			
		});
		return addReplyContainer;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newCommentContainer());
		
		repliesView = new RepeatingView("replies");
		List<CodeCommentReply> replies = new ArrayList<>(getComment().getReplies());
		replies.sort((reply1, reply2)->(int)(reply1.getId()-reply2.getId()));
		for (CodeCommentReply reply: replies) {
			repliesView.add(newReplyContainer(repliesView.newChildId(), reply.getId()));
		}
		add(repliesView);
		add(newAddReplyContainer());
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				CodeCommentPanel.class, "code-comment.css")));
	}

	private CodeComment getComment() {
		return getModelObject();
	}
	
	private CodeCommentReply getReply(Long replyId) {
		return GitPlex.getInstance(CodeCommentReplyManager.class).load(replyId);
	}
	
	protected abstract void onCommentDeleted(AjaxRequestTarget target);
	
	protected abstract CompareContext getCompareContext();
}
