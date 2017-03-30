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

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.PullRequestCommentManager;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestComment;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.component.comment.CommentInput;
import com.gitplex.server.web.component.comment.DepotAttachmentSupport;
import com.gitplex.server.web.component.link.AccountLink;
import com.gitplex.server.web.component.markdown.AttachmentSupport;
import com.gitplex.server.web.component.markdown.ContentVersionSupport;
import com.gitplex.server.web.component.markdown.MarkdownViewer;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.SinceChangesLink;
import com.gitplex.server.web.util.DateUtils;
import com.gitplex.server.web.util.ajaxlistener.ConfirmLeaveListener;
import com.gitplex.server.web.util.ajaxlistener.ConfirmListener;

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
		
		ContentVersionSupport contentVersionSuppport;
		if (SecurityUtils.canModify(getComment())) {
			contentVersionSuppport = new ContentVersionSupport() {

				@Override
				public long getVersion() {
					return getComment().getVersion();
				}
				
			};
		} else {
			contentVersionSuppport = null;
		}
		viewer.add(new MarkdownViewer("content", new IModel<String>() {

			@Override
			public String getObject() {
				return getComment().getContent();
			}

			@Override
			public void detach() {
			}

			@Override
			public void setObject(String object) {
				getComment().setContent(object);
				GitPlex.getInstance(PullRequestCommentManager.class).save(getComment());				
			}
			
		}, contentVersionSuppport));
		
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
				String autosaveKey = "autosave:editPullRequestComment:" + getComment().getId();
				CommentInput input = new CommentInput("input", Model.of(getComment().getContent()), false) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new DepotAttachmentSupport(getDepot(), getComment().getRequest().getUUID());
					}

					@Override
					protected String getAutosaveKey() {
						return autosaveKey;
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
							target.appendJavaScript(String.format("localStorage.removeItem('%s');", autosaveKey));
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
