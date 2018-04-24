package io.onedev.server.web.page.project.pullrequests.requestdetail.overview.activity;

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

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.comment.ProjectAttachmentSupport;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
class OpenedPanel extends GenericPanel<PullRequest> {

	public OpenedPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	private Component newViewer() {
		Fragment viewer = new Fragment("body", "viewFrag", this);
		
		String description = getPullRequest().getDescription();
		if (StringUtils.isNotBlank(description)) {
			ContentVersionSupport contentVersionSupport;
			if (SecurityUtils.canModify(getPullRequest())) {
				contentVersionSupport = new ContentVersionSupport() {

					@Override
					public long getVersion() {
						return getPullRequest().getVersion();
					}
					
				};
			} else {
				contentVersionSupport = null;
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
					OneDev.getInstance(PullRequestManager.class).save(getPullRequest());				
				}

			}, contentVersionSupport));
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
				
				String autosaveKey = "autosave:editPullRequestDescription:" + getPullRequest().getId(); 
				long lastVersion = getPullRequest().getVersion();
				CommentInput input = new CommentInput("input", Model.of(getPullRequest().getDescription()), false) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new ProjectAttachmentSupport(getPullRequest().getTargetProject(), getPullRequest().getUUID());
					}

					@Override
					protected String getAutosaveKey() {
						return autosaveKey;
					}

					@Override
					protected Project getProject() {
						return getPullRequest().getTargetProject();
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
							OneDev.getInstance(Dao.class).persist(getPullRequest());
	
							Component viewer = newViewer();
							editor.replaceWith(viewer);
							target.add(viewer);
							target.appendJavaScript(String.format("localStorage.removeItem('%s');", autosaveKey));
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
		
		PullRequest request = getPullRequest();
		add(new UserLink("user", User.getForDisplay(request.getSubmitter(), request.getSubmitterName())));
		add(new Label("age", DateUtils.formatAge(request.getSubmitDate())));
		
		add(newViewer());
	}

	private PullRequest getPullRequest() {
		return getModelObject();
	}
	
}
