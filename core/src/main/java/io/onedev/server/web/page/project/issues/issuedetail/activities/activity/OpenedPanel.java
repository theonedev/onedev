package io.onedev.server.web.page.project.issues.issuedetail.activities.activity;

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
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
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
class OpenedPanel extends GenericPanel<Issue> {

	private static final String BODY_ID = "body";
	
	public OpenedPanel(String id, IModel<Issue> model) {
		super(id, model);
	}
	
	private Component newViewer() {
		Fragment viewer = new Fragment(BODY_ID, "viewFrag", this);
		
		String description = getIssue().getDescription();
		if (StringUtils.isNotBlank(description)) {
			ContentVersionSupport contentVersionSupport;
			if (SecurityUtils.canModify(getIssue())) {
				contentVersionSupport = new ContentVersionSupport() {

					@Override
					public long getVersion() {
						return getIssue().getVersion();
					}
					
				};
			} else {
				contentVersionSupport = null;
			}
			viewer.add(new MarkdownViewer("content", new IModel<String>() {

				@Override
				public String getObject() {
					return getIssue().getDescription();
				}

				@Override
				public void detach() {
				}

				@Override
				public void setObject(String object) {
					getIssue().setDescription(object);
					OneDev.getInstance(IssueManager.class).save(getIssue());				
				}

			}, contentVersionSupport));
		} else {
			viewer.add(new Label("content", "<i>No description</i>").setEscapeModelStrings(false));
		}
		
		WebMarkupContainer actions = new WebMarkupContainer("actions");
		actions.setVisible(SecurityUtils.canModify(getIssue()));
		actions.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment editor = new Fragment(BODY_ID, "editFrag", OpenedPanel.this);
				
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				editor.add(form);
				
				NotificationPanel feedback = new NotificationPanel("feedback", form);
				feedback.setOutputMarkupPlaceholderTag(true);
				form.add(feedback);
				
				long lastVersion = getIssue().getVersion();
				CommentInput input = new CommentInput("input", Model.of(getIssue().getDescription()), false) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new ProjectAttachmentSupport(getIssue().getProject(), getIssue().getUUID());
					}

					@Override
					protected Project getProject() {
						return getIssue().getProject();
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
							if (getIssue().getVersion() != lastVersion)
								throw new StaleStateException("");
							OneDev.getInstance(IssueChangeManager.class).changeDescription(getIssue(), input.getModelObject());
	
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
		
		Issue issue = getIssue();
		add(new UserLink("user", User.getForDisplay(issue.getSubmitter(), issue.getSubmitterName())));
		add(new Label("age", DateUtils.formatAge(issue.getSubmitDate())));
		
		add(newViewer());
	}

	private Issue getIssue() {
		return getModelObject();
	}
	
}
