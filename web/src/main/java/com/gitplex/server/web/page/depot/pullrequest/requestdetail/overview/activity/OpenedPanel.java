package com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hibernate.StaleStateException;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.behavior.markdown.AttachmentSupport;
import com.gitplex.server.web.behavior.markdown.ResponsiveTaskBehavior;
import com.gitplex.server.web.component.comment.CommentInput;
import com.gitplex.server.web.component.comment.DepotAttachmentSupport;
import com.gitplex.server.web.component.link.AccountLink;
import com.gitplex.server.web.component.markdownviewer.MarkdownViewer;
import com.gitplex.server.web.util.DateUtils;
import com.gitplex.server.web.util.ajaxlistener.ConfirmLeaveListener;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
class OpenedPanel extends GenericPanel<PullRequest> {

	public OpenedPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	private Component newViewer() {
		Fragment viewer = new Fragment("body", "viewFrag", this);
		
		String description = getPullRequest().getDescription();
		if (StringUtils.isNotBlank(description)) {
			ResponsiveTaskBehavior responsiveTaskBehavior;
			if (SecurityUtils.canModify(getPullRequest())) {
				responsiveTaskBehavior = new ResponsiveTaskBehavior() {

					@Override
					public long getContentVersion() {
						return getPullRequest().getVersion();
					}
					
				};
			} else {
				responsiveTaskBehavior = null;
			}
			viewer.add(new MarkdownViewer("content", new IModel<String>() {

				@Override
				public String getObject() {
					return getPullRequest().getDescription();
				}

				@Override
				public void detach() {
				}

				@Override
				public void setObject(String object) {
					getPullRequest().setDescription(object);
					GitPlex.getInstance(PullRequestManager.class).save(getPullRequest());				
				}

			}, responsiveTaskBehavior));
		} else {
			viewer.add(new Label("content", "<i>No description</i>").setEscapeModelStrings(false));
		}
		
		WebMarkupContainer actions = new WebMarkupContainer("actions");
		actions.setVisible(SecurityUtils.canModify(getPullRequest()));
		actions.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment editor = new Fragment("body", "editFrag", OpenedPanel.this);
				
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				editor.add(form);
				
				NotificationPanel feedback = new NotificationPanel("feedback", form);
				feedback.setOutputMarkupPlaceholderTag(true);
				form.add(feedback);
				
				long lastVersion = getPullRequest().getVersion();
				CommentInput input = new CommentInput("input", Model.of(getPullRequest().getDescription())) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new DepotAttachmentSupport(getPullRequest().getTargetDepot(), 
								getPullRequest().getUUID());
					}

					@Override
					protected Depot getDepot() {
						return getPullRequest().getTargetDepot();
					}
					
				};
				form.add(input);
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(feedback);
					}

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						try {
							if (getPullRequest().getVersion() != lastVersion)
								throw new StaleStateException("");
							getPullRequest().setDescription(input.getModelObject());
							GitPlex.getInstance(Dao.class).persist(getPullRequest());
	
							Component viewer = newViewer();
							editor.replaceWith(viewer);
							target.add(viewer);
						} catch (StaleStateException e) {
							error("Some one changed the content you are editing. Reload the page and try again.");
							target.add(feedback);
						}
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

		viewer.add(actions);
		
		viewer.setOutputMarkupId(true);
		return viewer;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AccountLink("user", getPullRequest().getSubmitter()));
		add(new Label("age", DateUtils.formatAge(getPullRequest().getSubmitDate())));
		
		add(newViewer());
	}

	private PullRequest getPullRequest() {
		return getModelObject();
	}
	
}
