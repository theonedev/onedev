package com.gitplex.server.web.component.comment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
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
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.StaleStateException;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.manager.CodeCommentReplyManager;
import com.gitplex.server.manager.CodeCommentStatusChangeManager;
import com.gitplex.server.manager.VisitInfoManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.CodeCommentReply;
import com.gitplex.server.model.CodeCommentStatusChange;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.support.CodeCommentActivity;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.ClassUtils;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.component.link.AccountLink;
import com.gitplex.server.web.component.markdown.AttachmentSupport;
import com.gitplex.server.web.component.markdown.ContentVersionSupport;
import com.gitplex.server.web.component.markdown.MarkdownViewer;
import com.gitplex.server.web.page.depot.blob.DepotBlobPage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;
import com.gitplex.server.web.util.DateUtils;
import com.gitplex.server.web.util.ajaxlistener.ConfirmLeaveListener;
import com.gitplex.server.web.util.ajaxlistener.ConfirmListener;
import com.gitplex.server.web.websocket.PageDataChanged;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import jersey.repackaged.com.google.common.collect.Lists;

@SuppressWarnings("serial")
public abstract class CodeCommentPanel extends Panel {

	private final Long commentId;
	
	private RepeatingView activitiesView;
	
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

	private WebMarkupContainer newCommentContainer() {
		WebMarkupContainer commentContainer = new Fragment("comment", "viewFrag", this);
		commentContainer.setOutputMarkupId(true);
		
		commentContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getComment().isVisitedAfter(getComment().getDate())?"": "new";
			}
			
		}));
		
		Account userForDisplay = Account.getForDisplay(getComment().getUser(), getComment().getUserName());
		commentContainer.add(new AvatarLink("userAvatar", userForDisplay));
		commentContainer.add(new AccountLink("userName", userForDisplay));
		commentContainer.add(new Label("activityDescription", "commented"));
		commentContainer.add(new Label("activityDate", DateUtils.formatAge(getComment().getDate())));

		ContentVersionSupport contentVersionSupport;
		if (SecurityUtils.canModify(getComment())) {
			contentVersionSupport = new ContentVersionSupport() {

				@Override
				public long getVersion() {
					return getComment().getVersion();
				}
				
			};
		} else {
			contentVersionSupport = null;
		}
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
				GitPlex.getInstance(CodeCommentManager.class).save(comment);				
			}
			
		}, contentVersionSupport));

		WebMarkupContainer foot = new WebMarkupContainer("foot");
		foot.setVisible(SecurityUtils.canModify(getComment()));
		foot.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment(commentContainer.getId(), "commentEditFrag", CodeCommentPanel.this);
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				
				String autosaveKey = "autosave:editCodeComment:" + commentId;
				
				CommentInput contentInput = new CommentInput("content", Model.of(getComment().getContent()), true) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new DepotAttachmentSupport(getComment().getRequest().getTargetDepot(), getComment().getUUID());
					}

					@Override
					protected Depot getDepot() {
						return getComment().getRequest().getTargetDepot();
					}
					
					@Override
					protected String getAutosaveKey() {
						return autosaveKey;
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
								throw new StaleStateException("");
							comment.setContent(contentInput.getModelObject());
							GitPlex.getInstance(CodeCommentManager.class).save(comment);
							WebMarkupContainer commentContainer = newCommentContainer();
							fragment.replaceWith(commentContainer);
							target.add(commentContainer);
							onSaveComment(target, comment);
							target.appendJavaScript(String.format("localStorage.removeItem('%s');", autosaveKey));							
						} catch (StaleStateException e) {
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
	
	private WebMarkupContainer newActivityContainer(String componentId, CodeCommentActivity activity) {
		ActivityIdentity identity = new ActivityIdentity(activity);
		Fragment activityContainer = new Fragment(componentId, "viewFrag", this, Model.of(identity));
		activityContainer.setOutputMarkupId(true);
		activityContainer.setMarkupId(activity.getAnchor());
		activityContainer.add(AttributeAppender.append("name", activity.getAnchor()));
		
		Account userForDisplay = Account.getForDisplay(activity.getUser(), activity.getUserName());
		activityContainer.add(new AvatarLink("userAvatar", userForDisplay));
		activityContainer.add(new AccountLink("userName", userForDisplay));
		
		String activityDescription;
		if (activity instanceof CodeCommentStatusChange) {
			CodeCommentStatusChange statusChange = (CodeCommentStatusChange) activity;
			if (statusChange.isResolved())
				activityDescription = "resolved";
			else
				activityDescription = "unresolved";
		} else {
			activityDescription = "replied";
		}
		activityContainer.add(new Label("activityDescription", activityDescription));
		activityContainer.add(new Label("activityDate", DateUtils.formatAge(activity.getDate())));

		if (StringUtils.isNotBlank(activity.getNote())) {
			ContentVersionSupport contentVersionSupport;
			if (SecurityUtils.canModify(getComment())) {
				contentVersionSupport = new ContentVersionSupport() {
					
					@Override
					public long getVersion() {
						return identity.getActivity().getVersion();
					}

				};
			} else {
				contentVersionSupport = null;
			}
			activityContainer.add(new MarkdownViewer("content", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return identity.getActivity().getNote();
				}

				@Override
				public void setObject(String object) {
					if (identity.clazz == CodeCommentReply.class) {
						CodeCommentReply reply = (CodeCommentReply) activity;
						reply.setContent(object);
						GitPlex.getInstance(CodeCommentReplyManager.class).save(reply);				
					} else {
						CodeCommentStatusChange statusChange = (CodeCommentStatusChange) activity;
						statusChange.setNote(object);
						GitPlex.getInstance(CodeCommentStatusChangeManager.class).save(statusChange);				
					}
				}
				
			}, contentVersionSupport));			
			
		} else {
			activityContainer.add(new Label("content", "<div class='no-note'>No note</div>").setEscapeModelStrings(false));
		}
		
		WebMarkupContainer foot = new WebMarkupContainer("foot");
		foot.setVisible(SecurityUtils.canModify(activity));
		
		foot.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment(activityContainer.getId(), "activityEditFrag", 
						CodeCommentPanel.this, Model.of(identity.id));
				Form<?> form = new Form<Void>("form");
				String autosaveKey = "autosave:editCodeCommentActivity:" + identity.id;
				CommentInput contentInput = new CommentInput("content", Model.of(identity.getActivity().getNote()), 
						true) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new DepotAttachmentSupport(getComment().getRequest().getTargetDepot(), getComment().getUUID());
					}

					@Override
					protected Depot getDepot() {
						return getComment().getRequest().getTargetDepot();
					}

					@Override
					protected String getAutosaveKey() {
						return autosaveKey;
					}
					
				};
				contentInput.setRequired(identity.clazz == CodeCommentReply.class);
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
						WebMarkupContainer replyContainer = newActivityContainer(componentId, identity.getActivity());
						fragment.replaceWith(replyContainer);
						target.add(replyContainer);
					}
					
				});
				
				long lastVersion = identity.getActivity().getVersion();
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
							CodeCommentActivity activity = identity.getActivity();
							if (activity.getVersion() != lastVersion)
								throw new StaleStateException("");
							
							if (identity.clazz == CodeCommentReply.class) {
								CodeCommentReply reply = (CodeCommentReply) activity;
								reply.setContent(contentInput.getModelObject());
								GitPlex.getInstance(CodeCommentReplyManager.class).save(reply);				
							} else {
								CodeCommentStatusChange statusChange = (CodeCommentStatusChange) activity;
								statusChange.setNote(contentInput.getModelObject());
								GitPlex.getInstance(CodeCommentStatusChangeManager.class).save(statusChange);				
							}
							WebMarkupContainer activityContainer = newActivityContainer(componentId, identity.getActivity());
							fragment.replaceWith(activityContainer);
							target.add(activityContainer);
							target.appendJavaScript(String.format("localStorage.removeItem('%s');", autosaveKey));							
						} catch (StaleStateException e) {
							error("Someone changed the content you are editing. Reload the page and try again.");
							target.add(feedback);
						}
					}

				}.add(new Label("label", "Save")));
				
				fragment.add(form);
				fragment.setOutputMarkupId(true);
				activityContainer.replaceWith(fragment);
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
				activityContainer.remove();
				GitPlex.getInstance(CodeCommentReplyManager.class).delete((CodeCommentReply) identity.getActivity());
				String script = String.format("$('#%s').remove();", activityContainer.getMarkupId());
				target.appendJavaScript(script);
			}
			
		}.setVisible(activity instanceof CodeCommentReply));
		
		activityContainer.add(foot);		
		return activityContainer;			
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
				onAddReply(target, false, null);
			}
			
		});
		return addReplyContainer;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		PageParameters params = RequestChangesPage.paramsOf(getComment());
		add(new BookmarkablePageLink<Void>("outdatedContext", RequestChangesPage.class, params) {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PageDataChanged) {
					PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
					pageDataChanged.getHandler().add(this);
				}
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				CodeComment comment = getComment();
				if (getPage() instanceof DepotBlobPage) {
					setVisible(comment.isCodeChanged());
				} else if (getPage() instanceof RequestChangesPage) {
					RequestChangesPage page = (RequestChangesPage) getPage();
					if (page.getState().newCommit.equals(comment.getCommentPos().getCommit())) {
						setVisible(comment.isCodeChanged());
					} else {
						setVisible(!comment.getRequest().getHeadCommitHash().equals(page.getState().newCommit));
					}
				}
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		add(newCommentContainer());
		
		activitiesView = new RepeatingView("activities");
		
		List<CodeCommentActivity> activities = new ArrayList<>();
		for (CodeCommentReply reply: getComment().getReplies()) {
			activities.add(reply);
		}
		for (CodeCommentStatusChange statusChange: getComment().getStatusChanges()) {
			activities.add(statusChange);
		}

		activities.sort((o1, o2)->o1.getDate().compareTo(o2.getDate()));

		for (CodeCommentActivity activity: activities) {
			Component activityContainer = newActivityContainer(activitiesView.newChildId(), activity);				
			if (!getComment().isVisitedAfter(activity.getDate()))
				activityContainer.add(AttributeAppender.append("class", "new"));
			activitiesView.add(activityContainer);			
		}
		add(activitiesView);
		add(newAddReplyContainer());
		
		setOutputMarkupId(true);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PageDataChanged) {
			PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
			IPartialPageRequestHandler handler = pageDataChanged.getHandler();
			
			Date lastActivityDate;
			String prevActivityMarkupId;
			if (activitiesView.size() != 0) {
				@SuppressWarnings("deprecation")
				Component lastActivityContainer = activitiesView.get(activitiesView.size()-1);
				
				CodeCommentActivity lastActivity = ((ActivityIdentity) lastActivityContainer.getDefaultModelObject()).getActivity();
				lastActivityDate = lastActivity.getDate();
				prevActivityMarkupId = lastActivityContainer.getMarkupId();
			} else {
				lastActivityDate = getComment().getDate();
				prevActivityMarkupId = get("comment").getMarkupId();
			}
			
			List<CodeCommentActivity> activities = new ArrayList<>();
			for (CodeCommentReply reply: getComment().getReplies()) {
				if (reply.getDate().getTime()>lastActivityDate.getTime()) {
					activities.add(reply);
				}
			}
			for (CodeCommentStatusChange statusChange: getComment().getStatusChanges()) {
				if (statusChange.getDate().getTime()>lastActivityDate.getTime()) {
					activities.add(statusChange);
				}
			}
			activities.sort((o1, o2)->o1.getDate().compareTo(o2.getDate()));
			
			for (CodeCommentActivity activity: activities) {
				Component newActivityContainer = newActivityContainer(activitiesView.newChildId(), activity); 
				newActivityContainer.add(AttributeAppender.append("class", "new"));
				activitiesView.add(newActivityContainer);
				
				String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
						newActivityContainer.getMarkupId(), prevActivityMarkupId);
				handler.prependJavaScript(script);
				handler.add(newActivityContainer);
				prevActivityMarkupId = newActivityContainer.getMarkupId();
			}
			
			if (pageDataChanged.isOnConnect()) {
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
						if (SecurityUtils.getAccount() != null) {
							GitPlex.getInstance(VisitInfoManager.class).visit(SecurityUtils.getAccount(), getComment());
						}
					}
					
					@Override
					public void onDetach(RequestCycle cycle) {
					}
					
					@Override
					public void onBeginRequest(RequestCycle cycle) {
					}
				});
				
			}
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CodeCommentResourceReference()));
	}

	private void onAddReply(AjaxRequestTarget target, boolean changeStatus, @Nullable String placeholder) {
		Fragment fragment = new Fragment("addReply", "activityEditFrag", CodeCommentPanel.this);
		Form<?> form = new Form<Void>("form");

		String autosaveKey = "autosave:addCodeCommentReply:" + commentId;
		
		CommentInput contentInput = new CommentInput("content", Model.of(""), true) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new DepotAttachmentSupport(getComment().getRequest().getTargetDepot(), getComment().getUUID());
			}

			@Override
			protected Depot getDepot() {
				return getComment().getRequest().getTargetDepot();
			}

			@Override
			protected String getAutosaveKey() {
				return autosaveKey;
			}

			@Override
			protected List<AttributeModifier> getInputModifiers() {
				if (placeholder != null)
					return Lists.newArrayList(AttributeAppender.append("placeholder", placeholder));
				else
					return super.getInputModifiers();
			}
			
		};
		contentInput.setRequired(!changeStatus);
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

				Account user = SecurityUtils.getAccount();
				CodeComment comment = getComment();
				Date date = new Date();
				if (changeStatus) {
					CodeCommentStatusChange statusChange = new CodeCommentStatusChange();
					statusChange.setComment(getComment());
					statusChange.setUser(user);
					statusChange.setResolved(!comment.isResolved());
					statusChange.setDate(date);
					statusChange.setNote(contentInput.getModelObject());

					GitPlex.getInstance(CodeCommentManager.class).changeStatus(statusChange);				
					onStatusChanged(target, fragment, statusChange);
					onSaveComment(target, getComment());
				} else {
					CodeCommentReply reply = new CodeCommentReply();
					reply.setComment(comment);
					reply.setDate(date);
					reply.setUser(user);
					reply.setContent(contentInput.getModelObject());
					GitPlex.getInstance(CodeCommentReplyManager.class).save(reply, true);
					onReplyAdded(target, fragment, reply);
				}
				target.appendJavaScript(String.format("localStorage.removeItem('%s');", autosaveKey));							
			}

		};
		if (changeStatus) {
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
	
	public void onChangeStatus(AjaxRequestTarget target) {
		onAddReply(target, true, "Leave a note");
	}
	
	private void onStatusChanged(AjaxRequestTarget target, Fragment fragment, CodeCommentStatusChange statusChange) {
		WebMarkupContainer activityContainer = newActivityContainer(activitiesView.newChildId(), statusChange);
		activitiesView.add(activityContainer);

		String script = String.format("$('#%s .add-reply').before('<div id=\"%s\"></div>');", 
				CodeCommentPanel.this.getMarkupId(), activityContainer.getMarkupId());
		target.prependJavaScript(script);
		target.add(activityContainer);

		WebMarkupContainer addReplyContainer = newAddReplyContainer();
		fragment.replaceWith(addReplyContainer);
		target.add(addReplyContainer);
	}
	
	private void onReplyAdded(AjaxRequestTarget target, Fragment fragment, @Nullable CodeCommentReply reply) {
		if (reply != null) {
			WebMarkupContainer activityContainer = newActivityContainer(activitiesView.newChildId(), reply);
			activitiesView.add(activityContainer);
	
			String script = String.format("$('#%s .add-reply').before('<div id=\"%s\"></div>');", 
					CodeCommentPanel.this.getMarkupId(), activityContainer.getMarkupId());
			target.prependJavaScript(script);
			target.add(activityContainer);
		}
		
		WebMarkupContainer addReplyContainer = newAddReplyContainer();
		fragment.replaceWith(addReplyContainer);
		target.add(addReplyContainer);
	}
	
	protected abstract void onDeleteComment(AjaxRequestTarget target, CodeComment comment);
	
	protected abstract void onSaveComment(AjaxRequestTarget target, CodeComment comment);
	
	private static class ActivityIdentity implements Serializable {

		final Class<? extends CodeCommentActivity> clazz;
		
		final Long id;
		
		ActivityIdentity(CodeCommentActivity activity) {
			this.clazz = ClassUtils.unproxy(activity.getClass());
			this.id = activity.getId();
		}

		CodeCommentActivity getActivity() {
			return GitPlex.getInstance(Dao.class).load(clazz, id);
		}
		
	}
}
