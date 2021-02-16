package io.onedev.server.web.component.codecomment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.infomanager.UserInfoManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.asset.caret.CaretResourceReference;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.component.project.comment.CommentInput;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.util.ProjectAttachmentSupport;

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
		return OneDev.getInstance(CodeCommentManager.class).load(commentId);
	}

	private WebMarkupContainer newCommentContainer() {
		WebMarkupContainer commentContainer = new Fragment("comment", "viewFrag", this);
		commentContainer.setOutputMarkupId(true);
		
		commentContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getComment().isVisitedAfter(getComment().getCreateDate())?"": "new";
			}
			
		}));
		
		User user = User.from(getComment().getUser(), getComment().getUserName());
		commentContainer.add(new UserIdentPanel("userAvatar", user, Mode.AVATAR));
		commentContainer.add(new Label("userName", user.getDisplayName()));
		commentContainer.add(new Label("action", "commented"));
		commentContainer.add(new Label("date", DateUtils.formatAge(getComment().getCreateDate()))
				.add(new AttributeAppender("title", DateUtils.formatDateTime(getComment().getCreateDate()))));

		commentContainer.add(new MarkdownViewer("content", new IModel<String>() {

			@Override
			public String getObject() {
				return getComment().getContent();
			}

			@Override
			public void detach() {
			}

			@Override
			public void setObject(String object) {
				CodeComment comment = getComment();
				comment.setContent(object);
				OneDev.getInstance(CodeCommentManager.class).save(comment);				
			}
			
		}, null));

		WebMarkupContainer foot = new WebMarkupContainer("foot");
		foot.setVisible(SecurityUtils.canModifyOrDelete(getComment()));
		foot.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment(commentContainer.getId(), "commentEditFrag", CodeCommentPanel.this);
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				
				CommentInput contentInput = new CommentInput("content", Model.of(getComment().getContent()), true) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new ProjectAttachmentSupport(getComment().getProject(), getComment().getUUID(), 
								SecurityUtils.canManageCodeComments(getProject()));
					}

					@Override
					protected Project getProject() {
						return getComment().getProject();
					}

					@Override
					protected List<User> getMentionables() {
						return OneDev.getInstance(UserManager.class).queryAndSort(getComment().getParticipants());
					}
					
				};
				form.add(contentInput);
				contentInput.setRequired(true);
				contentInput.setLabel(Model.of("Comment"));
				
				FencedFeedbackPanel feedback = new FencedFeedbackPanel("feedback", form); 
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
						comment.setContent(contentInput.getModelObject());
						WebMarkupContainer commentContainer = newCommentContainer();
						fragment.replaceWith(commentContainer);
						target.add(commentContainer);
						onSaveComment(target, comment);
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
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(confirmMessage));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDeleteComment(target, getComment());
				OneDev.getInstance(CodeCommentManager.class).delete(getComment());
			}
			
		});
		
		commentContainer.add(foot);		
		return commentContainer;
	}
	
	private CodeCommentReply getReply(Long replyId) {
		return OneDev.getInstance(CodeCommentReplyManager.class).load(replyId);
	}
	
	private WebMarkupContainer newReplyContainer(String componentId, CodeCommentReply reply) {
		Long replyId = reply.getId();
		Fragment replyContainer = new Fragment(componentId, "viewFrag", this, Model.of(replyId));
		replyContainer.setOutputMarkupId(true);
		replyContainer.setMarkupId(reply.getAnchor());
		replyContainer.add(AttributeAppender.append("name", reply.getAnchor()));
		
		User user = User.from(reply.getUser(), reply.getUserName());
		replyContainer.add(new UserIdentPanel("userAvatar", user, Mode.AVATAR));
		replyContainer.add(new Label("userName", user.getDisplayName()));
		
		replyContainer.add(new Label("action", "replied"));
		replyContainer.add(new Label("date", DateUtils.formatAge(reply.getDate()))
				.add(new AttributeAppender("title", DateUtils.formatDateTime(reply.getDate()))));

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
				CodeCommentReply reply = getReply(replyId);
				reply.setContent(object);
				onSaveCommentReply(RequestCycle.get().find(AjaxRequestTarget.class), reply);
			}
			
		}, null));			
		
		WebMarkupContainer foot = new WebMarkupContainer("foot");
		foot.setVisible(SecurityUtils.canModifyOrDelete(reply));
		
		foot.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment(replyContainer.getId(), "replyEditFrag", CodeCommentPanel.this, 
						Model.of(replyId));
				Form<?> form = new Form<Void>("form");
				CommentInput contentInput = new CommentInput("content", Model.of(getReply(replyId).getContent()), true) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new ProjectAttachmentSupport(getProject(), getComment().getUUID(), 
								SecurityUtils.canManageCodeComments(getProject()));
					}

					@Override
					protected Project getProject() {
						return getComment().getProject();
					}

					@Override
					protected List<User> getMentionables() {
						return OneDev.getInstance(UserManager.class).queryAndSort(getComment().getParticipants());
					}

				};
				contentInput.setRequired(true);
				contentInput.setLabel(Model.of("Comment"));
				form.add(contentInput);
				
				FencedFeedbackPanel feedback = new FencedFeedbackPanel("feedback", form); 
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
						WebMarkupContainer replyContainer = newReplyContainer(componentId, getReply(replyId));
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
						onSaveCommentReply(target, reply);
						reply.setContent(contentInput.getModelObject());
						WebMarkupContainer replyContainer = newReplyContainer(componentId, reply);
						fragment.replaceWith(replyContainer);
						target.add(replyContainer);
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
				attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this reply?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				replyContainer.remove();
				OneDev.getInstance(CodeCommentReplyManager.class).delete(getReply(replyId));
				target.appendJavaScript(String.format("$('#%s').remove();", replyContainer.getMarkupId()));
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
				setVisible(SecurityUtils.getUser() != null);
			}
			
		};
		addReplyContainer.setOutputMarkupId(true);
		addReplyContainer.add(new AjaxLink<Void>("reply") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onAddReply(target);
			}
			
		});
		return addReplyContainer;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(newCommentContainer());
		
		repliesView = new RepeatingView("replies");

		List<CodeCommentReply> replies = new ArrayList<>();
		replies.addAll(getComment().getReplies());

		replies.sort((o1, o2)->o1.getDate().compareTo(o2.getDate()));

		for (CodeCommentReply reply: replies) {
			Component replyContainer = newReplyContainer(repliesView.newChildId(), reply);				
			if (!getComment().isVisitedAfter(reply.getDate()))
				replyContainer.add(AttributeAppender.append("class", "new"));
			repliesView.add(replyContainer);			
		}
		add(repliesView);
		add(newAddReplyContainer());
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				Date lastReplyDate;
				String prevReplyMarkupId;
				if (repliesView.size() != 0) {
					@SuppressWarnings("deprecation")
					Component lastReplyContainer = repliesView.get(repliesView.size()-1);
					
					CodeCommentReply lastReply = getReply((Long) lastReplyContainer.getDefaultModelObject());
					lastReplyDate = lastReply.getDate();
					prevReplyMarkupId = lastReplyContainer.getMarkupId();
				} else {
					lastReplyDate = getComment().getCreateDate();
					prevReplyMarkupId = get("comment").getMarkupId();
				}
				
				List<CodeCommentReply> replies = new ArrayList<>();
				for (CodeCommentReply reply: getComment().getReplies()) {
					if (reply.getDate().getTime()>lastReplyDate.getTime()) {
						replies.add(reply);
					}
				}
				replies.sort((o1, o2)->o1.getDate().compareTo(o2.getDate()));
				
				for (CodeCommentReply reply: replies) {
					Component newReplyContainer = newReplyContainer(repliesView.newChildId(), reply); 
					newReplyContainer.add(AttributeAppender.append("class", "new"));
					repliesView.add(newReplyContainer);
					
					String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
							newReplyContainer.getMarkupId(), prevReplyMarkupId);
					handler.prependJavaScript(script);
					handler.add(newReplyContainer);
					prevReplyMarkupId = newReplyContainer.getMarkupId();
				}
			}
			
			@Override
			public Collection<String> getObservables() {
				Set<String> observables = Sets.newHashSet(CodeComment.getWebSocketObservable(commentId));
				if (getPullRequest() != null)
					observables.add(PullRequest.getWebSocketObservable(getPullRequest().getId()));
				return observables;
			}
			
		});
		
		RequestCycle.get().getListeners().add(new IRequestCycleListener() {
			
			@Override
			public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
			}
			
			@Override
			public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler, Exception exception) {
			}
			
			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				return null;
			}
			
			@Override
			public void onEndRequest(RequestCycle cycle) {
				if (SecurityUtils.getUser() != null) {
					OneDev.getInstance(UserInfoManager.class).visitCodeComment(SecurityUtils.getUser(), getComment());
				}
			}
			
			@Override
			public void onDetach(RequestCycle cycle) {
			}
			
			@Override
			public void onBeginRequest(RequestCycle cycle) {
			}
		});		
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CaretResourceReference()));
		response.render(CssHeaderItem.forReference(new CodeCommentCssResourceReference()));
	}

	private void onAddReply(AjaxRequestTarget target) {
		Fragment fragment = new Fragment("addReply", "replyEditFrag", CodeCommentPanel.this);
		Form<?> form = new Form<Void>("form");

		String initialContent = "";
		
		if (getComment().getRequest() == null) {
			// automatically adds mentioning if the code comment is not associated with any pull requests, 
			// as otherwise no one will be aware of our comment
			List<CodeCommentReply> replies = new ArrayList<>(getComment().getReplies());
			Collections.sort(replies, new Comparator<CodeCommentReply>() {

				@Override
				public int compare(CodeCommentReply o1, CodeCommentReply o2) {
					return o2.getDate().compareTo(o1.getDate());
				}
				
			});
			for (CodeCommentReply reply: replies) {
				if (reply.getUser() != null && !reply.getUser().equals(SecurityUtils.getUser())) {
					initialContent = "@" + reply.getUser().getName() + " ";
					break;
				}
			}
			if (initialContent.length() == 0 
					&& getComment().getUser() != null 
					&& !getComment().getUser().equals(SecurityUtils.getUser())) {
				initialContent = "@" + getComment().getUser().getName() + " ";
			}
		}
		CommentInput contentInput = new CommentInput("content", Model.of(initialContent), true) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), getComment().getUUID(),
						SecurityUtils.canManageCodeComments(getProject()));
			}

			@Override
			protected Project getProject() {
				return getComment().getProject();
			}

			@Override
			protected List<User> getMentionables() {
				return OneDev.getInstance(UserManager.class).queryAndSort(getComment().getParticipants());
			}
			
		};
		contentInput.setRequired(true);
		contentInput.setLabel(Model.of("Comment"));
		form.add(contentInput);
		
		FencedFeedbackPanel feedback = new FencedFeedbackPanel("feedback", form); 
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

				User user = SecurityUtils.getUser();
				CodeComment comment = getComment();
				Date date = new Date();
				CodeCommentReply reply = new CodeCommentReply();
				reply.setComment(comment);
				reply.setDate(date);
				reply.setUser(user);
				reply.setContent(contentInput.getModelObject());
				
				onSaveCommentReply(target, reply);
				
				WebMarkupContainer replyContainer = newReplyContainer(repliesView.newChildId(), reply);
				repliesView.add(replyContainer);

				String script = String.format("$('#%s .add-reply').before('<div id=\"%s\"></div>');", 
						CodeCommentPanel.this.getMarkupId(), replyContainer.getMarkupId());
				target.prependJavaScript(script);
				target.add(replyContainer);
				
				WebMarkupContainer addReplyContainer = newAddReplyContainer();
				fragment.replaceWith(addReplyContainer);
				target.add(addReplyContainer);
			}

		};
		saveButton.add(new Label("label", "Save"));
		saveButton.add(AttributeAppender.append("class", "dirty-aware"));
		form.add(saveButton);
		
		fragment.add(form);
		fragment.setOutputMarkupId(true);
		get("addReply").replaceWith(fragment);
		target.add(fragment);	
		
		String script = String.format("$('#%s textarea').caret(%d);", 
				form.getMarkupId(), initialContent.length());
		target.appendJavaScript(script);
	}
	
	@Nullable
	protected abstract PullRequest getPullRequest();
	
	protected abstract void onDeleteComment(AjaxRequestTarget target, CodeComment comment);
	
	protected abstract void onSaveComment(AjaxRequestTarget target, CodeComment comment);

	protected abstract void onSaveCommentReply(AjaxRequestTarget target, CodeCommentReply reply);
	
}
