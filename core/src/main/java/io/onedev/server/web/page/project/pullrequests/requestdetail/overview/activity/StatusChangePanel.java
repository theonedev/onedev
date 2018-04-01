package io.onedev.server.web.page.project.pullrequests.requestdetail.overview.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.hibernate.StaleStateException;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestStatusChangeManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestStatusChange;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.comment.ProjectAttachmentSupport;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.page.project.pullrequests.requestdetail.overview.SinceChangesLink;
import io.onedev.server.web.util.DateUtils;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
class StatusChangePanel extends GenericPanel<PullRequestStatusChange> {

	public StatusChangePanel(String id, IModel<PullRequestStatusChange> model) {
		super(id, model);
	}

	private Component newViewer() {
		Fragment viewer = new Fragment("body", "viewFrag", this);
		
		String note = getStatusChange().getNote();
		if (StringUtils.isNotBlank(note)) {
			ContentVersionSupport contentVersionSupport;
			if (SecurityUtils.canModify(getStatusChange())) {
				contentVersionSupport = new ContentVersionSupport() {

					@Override
					public long getVersion() {
						return getStatusChange().getVersion();
					}
					
				};
			} else {
				contentVersionSupport = null;
			}
			viewer.add(new MarkdownViewer("content", new IModel<String>() {

				@Override
				public String getObject() {
					return getStatusChange().getNote();
				}

				@Override
				public void detach() {
				}

				@Override
				public void setObject(String object) {
					getStatusChange().setNote(object);
					OneDev.getInstance(PullRequestStatusChangeManager.class).save(getStatusChange());				
				}

			}, contentVersionSupport));
		} else {
			viewer.add(new Label("content", "<i>No note</i>").setEscapeModelStrings(false));
		}
		
		WebMarkupContainer actions = new WebMarkupContainer("actions");
		actions.setVisible(SecurityUtils.canModify(getStatusChange()));
		actions.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment editor = new Fragment("body", "editFrag", StatusChangePanel.this);
				
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				editor.add(form);
				
				NotificationPanel feedback = new NotificationPanel("feedback", form);
				feedback.setOutputMarkupPlaceholderTag(true);
				form.add(feedback);
				
				String autosaveKey = "autosave:editPullRequestStatusChange:" + getStatusChange().getId();
				long lastVersion = getStatusChange().getVersion();
				CommentInput input = new CommentInput("input", Model.of(getStatusChange().getNote()), false) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new ProjectAttachmentSupport(getStatusChange().getRequest().getTargetProject(), 
								getStatusChange().getRequest().getUUID());
					}

					@Override
					protected Project getProject() {
						return getStatusChange().getRequest().getTargetProject();
					}
					
					@Override
					protected String getAutosaveKey() {
						return autosaveKey;
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
							if (getStatusChange().getVersion() != lastVersion)
								throw new StaleStateException("");
							getStatusChange().setNote(input.getModelObject());
							OneDev.getInstance(PullRequestStatusChangeManager.class).save(getStatusChange());
	
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
		
		PullRequestStatusChange statusChange = getModelObject();
		
		WebMarkupContainer container = new WebMarkupContainer("statusChange");
		String activityName = statusChange.getType().getName();
		container.add(AttributeAppender.append("class", "activity " + activityName.replace(" ", "-").replace("(", "").replace(")", "").toLowerCase()));
		
		WebMarkupContainer icon = new WebMarkupContainer("icon");
		container.add(icon);
		String iconClass = statusChange.getType().getIconClass();
		if (iconClass != null)
			icon.add(AttributeAppender.append("class", iconClass));
		
		User userForDisplay = User.getForDisplay(statusChange.getUser(), statusChange.getUserName());
		container.add(new AvatarLink("userAvatar", userForDisplay));
		container.add(new UserLink("userName", userForDisplay));
		container.add(new Label("eventName", statusChange.getType().getName()));
		container.add(new Label("eventDate", DateUtils.formatAge(statusChange.getDate())));
		container.add(new SinceChangesLink("changes", new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return StatusChangePanel.this.getModelObject().getRequest();
			}
			
		}, statusChange.getDate()));
		
		container.add(newViewer());
		
		add(container);
	}

	private PullRequestStatusChange getStatusChange() {
		return getModelObject();
	}
}
