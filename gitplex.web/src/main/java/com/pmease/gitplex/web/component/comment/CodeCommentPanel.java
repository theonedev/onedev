package com.pmease.gitplex.web.component.comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
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
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.ajaxlistener.ConfirmListener;
import com.pmease.commons.wicket.behavior.markdown.AttachmentSupport;
import com.pmease.commons.wicket.component.markdownviewer.MarkdownViewer;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.CodeCommentStatusChange;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.support.CodeCommentActivity;
import com.pmease.gitplex.core.entity.support.CompareContext;
import com.pmease.gitplex.core.entity.support.DepotAndRevision;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.CodeCommentReplyManager;
import com.pmease.gitplex.core.manager.CodeCommentStatusChangeManager;
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

			Date lastActivityDate;
			String prevActivityMarkupId;
			if (activitiesView.size() != 0) {
				@SuppressWarnings("deprecation")
				Component lastReplyContainer = activitiesView.get(activitiesView.size()-1);
				CodeCommentReply lastReply = GitPlex.getInstance(CodeCommentReplyManager.class)
						.load((Long) lastReplyContainer.getDefaultModelObject());
				lastActivityDate = lastReply.getDate();
				prevActivityMarkupId = lastReplyContainer.getMarkupId();
			} else {
				lastActivityDate = getComment().getCreateDate();
				prevActivityMarkupId = get("comment").getMarkupId();
			}
			
			PullRequest request = getPullRequest();
			List<CodeCommentActivity> activities = new ArrayList<>();
			for (CodeCommentReply reply: getComment().getReplies()) {
				if (reply.getDate().after(lastActivityDate) 
						&& (request == null || request.getRequestComparingInfo(reply.getComparingInfo()) != null)) {
					activities.add(reply);
				}
			}
			for (CodeCommentStatusChange statusChange: getComment().getStatusChanges()) {
				if (statusChange.getDate().after(lastActivityDate) 
						&& (request == null || request.getRequestComparingInfo(statusChange.getComparingInfo()) != null)) {
					activities.add(statusChange);
				}
			}
			activities.sort((o1, o2)->o1.getDate().compareTo(o2.getDate()));
			
			for (CodeCommentActivity activity: activities) {
				Component newActivityContainer = newActivityContainer(activitiesView.newChildId(), activity); 
				activitiesView.add(newActivityContainer);
				
				String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
						newActivityContainer.getMarkupId(), prevActivityMarkupId);
				partialPageRequestHandler.prependJavaScript(script);
				partialPageRequestHandler.add(newActivityContainer);
				prevActivityMarkupId = newActivityContainer.getMarkupId();
			}
		}
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
		
		commentContainer.add(new AvatarLink("userAvatar", getComment().getUser()));
		commentContainer.add(new AccountLink("userName", getComment().getUser()));
		commentContainer.add(new Label("activityDescription", "commented"));
		commentContainer.add(new Label("activityDate", DateUtils.formatAge(getComment().getCreateDate())));
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
					commentContainer.warn("Someone changed the content you are editing. "
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
	
	private WebMarkupContainer newActivityContainer(String componentId, CodeCommentActivity activity) {
		Class<? extends CodeCommentActivity> activityClass = activity.getClass();
		Long activityId = activity.getId();
		Fragment activityContainer = new Fragment(componentId, "viewFrag", this, Model.of(activityId));
		activityContainer.setOutputMarkupId(true);
		
		activityContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getComment().isVisitedAfter(getActivity(activityClass, activityId).getDate())?"":"new";
			}
			
		}));
		
		activityContainer.add(new AvatarLink("userAvatar", activity.getUser()));
		activityContainer.add(new AccountLink("userName", activity.getUser()));
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
		activityContainer.add(new Link<Void>("compareContext") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getCompareContext(getComment()).equals(getActivity(activityClass, activityId).getCompareContext()));
			}

			@Override
			public void onClick() {
				CodeCommentActivity activity = getActivity(activityClass, activityId);
				CompareContext compareContext = activity.getCompareContext();
				PullRequest request = getPullRequest();
				if (request != null) {
					RequestChangesPage.State state = new RequestChangesPage.State();
					state.commentId = activity.getComment().getId();
					if (compareContext.isLeftSide()) {
						state.oldCommit = compareContext.getCompareCommit();
						state.newCommit = activity.getComment().getCommentPos().getCommit();
					} else {
						state.oldCommit = activity.getComment().getCommentPos().getCommit();
						state.newCommit = compareContext.getCompareCommit();
					}
					state.mark = activity.getComment().getCommentPos();
					state.pathFilter = compareContext.getPathFilter();
					state.whitespaceOption = compareContext.getWhitespaceOption();
					PageParameters params  = RequestChangesPage.paramsOf(request, state);
					setResponsePage(RequestChangesPage.class, params);
				} else {
					RevisionComparePage.State state = new RevisionComparePage.State();
					CodeComment comment = activity.getComment();
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

		NotificationPanel feedback = new NotificationPanel("feedback", activityContainer);
		feedback.setOutputMarkupPlaceholderTag(true);
		activityContainer.add(feedback);
		
		if (StringUtils.isNotBlank(activity.getNote())) {
			AtomicLong lastVersionRef = new AtomicLong(activity.getVersion());
			activityContainer.add(new MarkdownViewer("content", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return getActivity(activityClass, activityId).getNote();
				}

				@Override
				public void setObject(String object) {
					AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
					Preconditions.checkNotNull(target);
					CodeCommentActivity activity = getActivity(activityClass, activityId);
					try {
						if (activity.getVersion() != lastVersionRef.get())
							throw new StaleObjectStateException(activityClass.getName(), activity.getId());
						if (activityClass == CodeCommentReply.class) {
							CodeCommentReply reply = (CodeCommentReply) activity;
							reply.setContent(object);
							GitPlex.getInstance(CodeCommentReplyManager.class).save(reply);				
						} else {
							CodeCommentStatusChange statusChange = (CodeCommentStatusChange) activity;
							statusChange.setNote(object);
							GitPlex.getInstance(CodeCommentStatusChangeManager.class).save(statusChange);				
						}
						target.add(feedback); // clear the feedback
					} catch (StaleObjectStateException e) {
						activityContainer.warn("Some one changed the content you are editing. The content has now been reloaded, "
								+ "please try again.");
						target.add(activityContainer);
					}
					lastVersionRef.set(activity.getVersion());
				}
				
			}, SecurityUtils.canModify(getComment())));			
		} else {
			activityContainer.add(new Label("content", "<div class='no-note'>No note</div>").setEscapeModelStrings(false));
		}
		
		WebMarkupContainer foot = new WebMarkupContainer("foot");
		foot.setVisible(SecurityUtils.canModify(activity));
		
		foot.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment(activityContainer.getId(), "noteEditFrag", 
						CodeCommentPanel.this, Model.of(activityId));
				Form<?> form = new Form<Void>("form");
				CommentInput contentInput = new CommentInput("content", Model.of(getActivity(activityClass, activityId).getNote())) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new DepotAttachmentSupport(getComment().getDepot(), getComment().getUUID());
					}

					@Override
					protected Depot getDepot() {
						return getComment().getDepot();
					}
					
				};
				contentInput.setRequired(activityClass == CodeCommentReply.class);
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
						WebMarkupContainer replyContainer = newActivityContainer(componentId, getActivity(activityClass, activityId));
						fragment.replaceWith(replyContainer);
						target.add(replyContainer);
					}
					
				});
				
				long lastVersion = getActivity(activityClass, activityId).getVersion();
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
							CodeCommentActivity activity = getActivity(activityClass, activityId);
							if (activity.getVersion() != lastVersion)
								throw new StaleObjectStateException(activityClass.getName(), activity.getId());
							
							if (activityClass == CodeCommentReply.class) {
								CodeCommentReply reply = (CodeCommentReply) activity;
								reply.setContent(contentInput.getModelObject());
								GitPlex.getInstance(CodeCommentReplyManager.class).save(reply);				
							} else {
								CodeCommentStatusChange statusChange = (CodeCommentStatusChange) activity;
								statusChange.setNote(contentInput.getModelObject());
								GitPlex.getInstance(CodeCommentStatusChangeManager.class).save(statusChange);				
							}
							WebMarkupContainer activityContainer = newActivityContainer(componentId, getActivity(activityClass, activityId));
							fragment.replaceWith(activityContainer);
							target.add(activityContainer);
						} catch (StaleObjectStateException e) {
							error("Some one changed the content you are editing. Reload the page and try again.");
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
				GitPlex.getInstance(CodeCommentReplyManager.class).delete((CodeCommentReply) getActivity(activityClass, activityId));
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
				onAddReply(target, false);
			}
			
		});
		return addReplyContainer;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newCommentContainer());
		
		activitiesView = new RepeatingView("activities");
		PullRequest request = getPullRequest();
		
		List<CodeCommentActivity> activities = new ArrayList<>();
		for (CodeCommentReply reply: getComment().getReplies()) {
			if (request == null || request.getRequestComparingInfo(reply.getComparingInfo()) != null) {
				activities.add(reply);
			}
		}
		for (CodeCommentStatusChange statusChange: getComment().getStatusChanges()) {
			if (request == null || request.getRequestComparingInfo(statusChange.getComparingInfo()) != null) {
				activities.add(statusChange);
			}
		}

		activities.sort((o1, o2)->o1.getDate().compareTo(o2.getDate()));

		for (CodeCommentActivity activity: activities) {
			activitiesView.add(newActivityContainer(activitiesView.newChildId(), activity));				
		}
		add(activitiesView);
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

	private CodeCommentActivity getActivity(Class<? extends CodeCommentActivity> entityClass, Long activityId) {
		return GitPlex.getInstance(Dao.class).load(entityClass, activityId);
	}
	
	private void onAddReply(AjaxRequestTarget target, boolean changeStatus) {
		Fragment fragment = new Fragment("addReply", "noteEditFrag", CodeCommentPanel.this);
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
				CompareContext compareContext = getCompareContext(comment);
				if (changeStatus) {
					CodeCommentStatusChange statusChange = new CodeCommentStatusChange();
					statusChange.setComment(getComment());
					statusChange.setUser(user);
					statusChange.setCompareContext(compareContext);
					statusChange.setResolved(!comment.isResolved());
					statusChange.setDate(date);

					GitPlex.getInstance(CodeCommentManager.class).changeStatus(statusChange);				
					onStatusChanged(target, fragment, statusChange);
					onSaveComment(target, getComment());
				} else {
					CodeCommentReply reply = new CodeCommentReply();
					reply.setComment(comment);
					reply.setDate(date);
					reply.setUser(user);
					reply.setContent(contentInput.getModelObject());
					reply.setCompareContext(compareContext);
					GitPlex.getInstance(CodeCommentReplyManager.class).save(reply);
					onReplyAdded(target, fragment, reply);
				}
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
		onAddReply(target, true);
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
	
	protected abstract CompareContext getCompareContext(CodeComment comment);
	
	@Nullable
	protected abstract PullRequest getPullRequest();
}
