package com.pmease.gitplex.web.component.comment;

import java.util.ArrayList;
import java.util.List;

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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.ajaxlistener.ConfirmListener;
import com.pmease.commons.wicket.behavior.markdown.AttachmentSupport;
import com.pmease.commons.wicket.component.markdownviewer.MarkdownViewer;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.CodeCommentReplyManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
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
		commentContainer.add(new Label("authorDate", DateUtils.formatAge(getComment().getDate())));

		commentContainer.add(new MarkdownViewer("body", Model.of(getComment().getContent()), true));

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
						return new DepotAttachmentSupport(getComment().getDepot());
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
						WebMarkupContainer commentContainer = newCommentContainer();
						fragment.replaceWith(commentContainer);
						target.add(commentContainer);
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

						CodeComment comment = getComment();
						comment.setContent(input.getModelObject());
						GitPlex.getInstance(CodeCommentManager.class).persist(comment);
						WebMarkupContainer commentContainer = newCommentContainer();
						fragment.replaceWith(commentContainer);
						target.add(commentContainer);
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
				GitPlex.getInstance(CodeCommentManager.class).remove(getComment());
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

		replyContainer.add(new MarkdownViewer("body", Model.of(getReply(replyId).getContent()), true));

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
						return new DepotAttachmentSupport(getComment().getDepot());
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
						WebMarkupContainer replyContainer = newReplyContainer(componentId, replyId);
						fragment.replaceWith(replyContainer);
						target.add(replyContainer);
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

						CodeCommentReply reply = getReply(replyId);
						reply.setContent(input.getModelObject());
						GitPlex.getInstance(CodeCommentReplyManager.class).persist(reply);

						WebMarkupContainer replyContainer = newReplyContainer(componentId, replyId);
						fragment.replaceWith(replyContainer);
						target.add(replyContainer);
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
				GitPlex.getInstance(CodeCommentReplyManager.class).remove(getReply(replyId));
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
						return new DepotAttachmentSupport(getComment().getDepot());
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
						GitPlex.getInstance(CodeCommentReplyManager.class).persist(reply);
						
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
		
		add(AttributeAppender.append("data-line", getComment().getMark().getBeginLine()));
		add(AttributeAppender.append("data-comment", getComment().getId()));
		
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
}
