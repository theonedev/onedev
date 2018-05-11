package io.onedev.server.web.page.project.issues.issuedetail.activity;

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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hibernate.StaleStateException;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueCommentManager;
import io.onedev.server.model.IssueComment;
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
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;

@SuppressWarnings("serial")
class CommentedPanel extends GenericPanel<IssueComment> {

	public CommentedPanel(String id, IModel<IssueComment> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new UserLink("user", User.getForDisplay(getComment().getUser(), getComment().getUserName())));
		add(new Label("age", DateUtils.formatAge(getComment().getDate())));
		
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
				OneDev.getInstance(IssueCommentManager.class).save(getComment());				
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
				String autosaveKey = "autosave:editIssueComment:" + getComment().getId();
				CommentInput input = new CommentInput("input", Model.of(getComment().getContent()), false) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new ProjectAttachmentSupport(getProject(), getComment().getIssue().getUUID());
					}

					@Override
					protected String getAutosaveKey() {
						return autosaveKey;
					}
					
					@Override
					protected Project getProject() {
						return getComment().getIssue().getProject();
					}
					
				};
				input.setRequired(true).setLabel(Model.of("Comment"));
				form.add(input);

				final long lastVersion = getComment().getVersion();
				form.add(new AjaxSubmitLink("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						IssueComment comment = getComment();
						try {
							if (comment.getVersion() != lastVersion)
								throw new StaleStateException("");
							comment.setContent(input.getModelObject());
							OneDev.getInstance(IssueCommentManager.class).save(comment);
	
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
				OneDev.getInstance(IssueCommentManager.class).delete(getComment());
				send(CommentedPanel.this, Broadcast.BUBBLE, new IssueCommentDeleted(target));
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
	
	private IssueComment getComment() {
		return getModelObject();
	}
	
}
