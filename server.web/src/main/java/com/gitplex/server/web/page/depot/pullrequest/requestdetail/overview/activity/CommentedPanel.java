package com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.activity;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hibernate.StaleStateException;

import com.gitplex.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.gitplex.commons.wicket.ajaxlistener.ConfirmListener;
import com.gitplex.commons.wicket.behavior.markdown.AttachmentSupport;
import com.gitplex.commons.wicket.component.markdown.MarkdownEditSupport;
import com.gitplex.commons.wicket.component.markdown.MarkdownPanel;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.entity.PullRequestComment;
import com.gitplex.server.core.manager.PullRequestCommentManager;
import com.gitplex.server.core.security.SecurityUtils;
import com.gitplex.server.web.component.AccountLink;
import com.gitplex.server.web.component.comment.CommentInput;
import com.gitplex.server.web.component.comment.DepotAttachmentSupport;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.SinceChangesLink;
import com.gitplex.server.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
class CommentedPanel extends GenericPanel<PullRequestComment> {

	public CommentedPanel(String id, IModel<PullRequestComment> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new AccountLink("user", getComment().getUser()));
		add(new Label("age", DateUtils.formatAge(getComment().getDate())));
		
		add(new SinceChangesLink("changes", new AbstractReadOnlyModel<PullRequest>() {

			@Override
			public PullRequest getObject() {
				return getComment().getRequest();
			}

		}, getComment().getDate()));
		
		add(newViewer());

		setOutputMarkupId(true);
	}
	
	private Component newViewer() {
		Fragment viewer = new Fragment("body", "viewFrag", this);
		
		MarkdownEditSupport editSupport;
		if (SecurityUtils.canModify(getComment())) {
			editSupport = new MarkdownEditSupport() {

				@Override
				public void setContent(String content) {
					getComment().setContent(content);
					GitPlex.getInstance(PullRequestCommentManager.class).save(getComment());				
				}

				@Override
				public long getVersion() {
					return getComment().getVersion();
				}
				
			};
		} else {
			editSupport = null;
		}
		viewer.add(new MarkdownPanel("content", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getComment().getContent();
			}
			
		}, editSupport));
		
		WebMarkupContainer actions = new WebMarkupContainer("actions");
		actions.setVisible(SecurityUtils.canModify(getComment()));
		
		actions.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment editor = new Fragment("body", "editFrag", CommentedPanel.this);

				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				editor.add(form);
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
								throw new StaleStateException("");
							comment.setContent(input.getModelObject());
							GitPlex.getInstance(PullRequestCommentManager.class).save(comment);
	
							Component viewer = newViewer();
							editor.replaceWith(viewer);
							target.add(viewer);
						} catch (StaleStateException e) {
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
						Component viewer = newViewer();
						editor.replaceWith(viewer);
						target.add(viewer);
					}
					
				});
				
				editor.setOutputMarkupId(true);
				viewer.replaceWith(editor);
				target.add(editor);
			}

		});
		
		actions.add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				GitPlex.getInstance(PullRequestCommentManager.class).delete(getComment());
				send(CommentedPanel.this, Broadcast.BUBBLE, new RequestCommentDeleted(target));
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this comment?"));
			}

		});
				
		viewer.add(actions);
		viewer.setOutputMarkupId(true);
		
		return viewer;
	}
	
	private PullRequestComment getComment() {
		return getModelObject();
	}
	
}
