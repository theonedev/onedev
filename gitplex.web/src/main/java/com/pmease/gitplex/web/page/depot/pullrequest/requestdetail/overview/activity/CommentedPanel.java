package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.StaleObjectStateException;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.ajaxlistener.ConfirmListener;
import com.pmease.commons.wicket.behavior.markdown.AttachmentSupport;
import com.pmease.commons.wicket.component.markdownviewer.MarkdownViewer;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.comment.DepotAttachmentSupport;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.SinceChangesLink;
import com.pmease.gitplex.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
class CommentedPanel extends ActivityPanel {

	private final IModel<PullRequestComment> commentModel = new LoadableDetachableModel<PullRequestComment>(){

		@Override
		protected PullRequestComment load() {
			return ((CommentedRenderer)renderer).getComment();
		}
		
	};
	
	public CommentedPanel(String id, CommentedRenderer activity) {
		super(id, activity);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer head = new WebMarkupContainer("head"); 
		head.setOutputMarkupId(true);
		add(head);
		
		head.add(new AccountLink("user", getComment().getUser()));
		head.add(new Label("age", DateUtils.formatAge(getComment().getDate())));

		head.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("body", "editFrag", CommentedPanel.this);

				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				fragment.add(form);
				NotificationPanel feedback = new NotificationPanel("feedback", form); 
				feedback.setOutputMarkupPlaceholderTag(true);
				form.add(feedback);
				CommentInput input = new CommentInput("input", Model.of(getComment().getContent())) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new DepotAttachmentSupport(getDepot(), getComment().getRequest().getUUID());
					}

					@Override
					protected Depot getDepot() {
						return getComment().getDepot();
					}
					
				};
				input.setRequired(true);
				form.add(input);

				final long lastVersion = getComment().getVersion();
				form.add(new AjaxSubmitLink("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						PullRequestComment comment = getComment();
						try {
							if (comment.getVersion() != lastVersion)
								throw new StaleObjectStateException(PullRequestComment.class.getName(), comment.getId());
							comment.setContent(input.getModelObject());
							GitPlex.getInstance(PullRequestCommentManager.class).save(comment);
	
							WebMarkupContainer viewer = newViewer();
							CommentedPanel.this.replace(viewer);
							target.add(viewer);
							target.add(head);
						} catch (StaleObjectStateException e) {
							error("Some one changed the content you are editing. Reload the page and try again.");
							target.add(feedback);
						}
					}
					
					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(feedback);
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(form));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						WebMarkupContainer viewer = newViewer();
						CommentedPanel.this.replace(viewer);
						target.add(viewer);
						target.add(head);
					}
					
				});
				
				fragment.setOutputMarkupId(true);
				CommentedPanel.this.replace(fragment);
				target.add(fragment);
				target.add(head);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(SecurityUtils.canModify(getComment()) && CommentedPanel.this.get("body").get("form") == null);
			}

		});
		
		head.add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				GitPlex.getInstance(PullRequestCommentManager.class).delete(getComment());
				send(CommentedPanel.this, Broadcast.BUBBLE, new CommentRemoved(target, getComment()));
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this comment?"));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(SecurityUtils.canModify(getComment()));
			}

		});
		
		head.add(new SinceChangesLink("changes", requestModel, getComment().getDate()));
		
		add(newViewer());

		setOutputMarkupId(true);
	}
	
	private WebMarkupContainer newViewer() {
		WebMarkupContainer viewer = new Fragment("body", "viewFrag", this);
		viewer.setOutputMarkupId(true);
		
		NotificationPanel feedback = new NotificationPanel("feedback", viewer);
		feedback.setOutputMarkupPlaceholderTag(true);
		viewer.add(feedback);
		AtomicLong lastVersionRef = new AtomicLong(getComment().getVersion());
		viewer.add(new MarkdownViewer("content", new IModel<String>() {

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
				PullRequestComment comment = getComment();
				try {
					if (comment.getVersion() != lastVersionRef.get())
						throw new StaleObjectStateException(CodeComment.class.getName(), comment.getId());
					comment.setContent(object);
					GitPlex.getInstance(PullRequestCommentManager.class).save(comment);				
					target.add(feedback); // clear the feedback
				} catch (StaleObjectStateException e) {
					viewer.warn("Some one changed the content you are editing. "
							+ "The content has now been reloaded, please try again.");
					target.add(viewer);
				}
				lastVersionRef.set(comment.getVersion());
			}
			
		}, SecurityUtils.canModify(getComment())));

		return viewer;		
	}
	
	private PullRequestComment getComment() {
		return commentModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		commentModel.detach();
		super.onDetach();
	}

}
