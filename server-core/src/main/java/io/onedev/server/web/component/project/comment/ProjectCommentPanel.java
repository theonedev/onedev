package io.onedev.server.web.component.project.comment;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.StaleStateException;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.util.DeleteCallback;

@SuppressWarnings("serial")
public abstract class ProjectCommentPanel extends Panel {

	private static final String COMMENT_ID = "comment";
	
	public ProjectCommentPanel(String id) {
		super(id);
	}
	
	private long getVersion() {
		if (getContentVersionSupport() != null)
			return getContentVersionSupport().getVersion();
		else
			return 0;
	}
	
	private Component newViewer() {
		Fragment viewer = new Fragment(COMMENT_ID, "viewFrag", this);
		
		if (StringUtils.isNotBlank(getComment())) {
			viewer.add(new MarkdownViewer("content", new IModel<String>() {

				@Override
				public String getObject() {
					return getComment();
				}

				@Override
				public void detach() {
				}

				@Override
				public void setObject(String object) {
					onSaveComment(RequestCycle.get().find(AjaxRequestTarget.class), object);
				}

			}, getContentVersionSupport()));
		} else {
			viewer.add(new Label("content", "<i class='text-muted'>No comment</i>").setEscapeModelStrings(false));
		}
		
		WebMarkupContainer actions = new WebMarkupContainer("actions");
		actions.setVisible(canModifyOrDeleteComment());
		actions.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment editor = new Fragment(COMMENT_ID, "editFrag", ProjectCommentPanel.this);
				
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				editor.add(form);
				
				FencedFeedbackPanel feedback = new FencedFeedbackPanel("feedback", form); 
				feedback.setOutputMarkupPlaceholderTag(true);
				form.add(feedback);
				
				long lastVersion = getVersion();
				CommentInput input = new CommentInput("input", Model.of(getComment()), false) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return ProjectCommentPanel.this.getAttachmentSupport();
					}

					@Override
					protected Project getProject() {
						return ProjectCommentPanel.this.getProject();
					}

					@Override
					protected List<User> getMentionables() {
						return ProjectCommentPanel.this.getMentionables();
					}
					
				};
				input.setRequired(getRequiredLabel() != null).setLabel(Model.of(getRequiredLabel()));
				form.add(input);
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						try {
							if (lastVersion != getVersion()) 
								throw new StaleStateException("");
							onSaveComment(target, input.getModelObject());
							Component viewer = newViewer();
							editor.replaceWith(viewer);
							target.add(viewer);
						} catch (StaleStateException e) {
							warn("Some one changed the content you are editing. Reload the page and try again.");
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
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this comment?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getDeleteCallback().onDelete(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getDeleteCallback() != null);
			}
			
		});

		viewer.add(actions);
		
		viewer.setOutputMarkupId(true);
		return viewer;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newViewer());
	}

	@Nullable
	protected abstract String getComment();
	
	protected abstract void onSaveComment(AjaxRequestTarget target, @Nullable String comment);
	
	protected abstract Project getProject();
	
	protected abstract AttachmentSupport getAttachmentSupport();
	
	protected abstract boolean canModifyOrDeleteComment();
	
	@Nullable
	protected abstract String getRequiredLabel();
	
	protected abstract ContentVersionSupport getContentVersionSupport();
	
	@Nullable
	protected abstract DeleteCallback getDeleteCallback();
	
	protected List<User> getMentionables() {
		return OneDev.getInstance(UserManager.class).queryAndSort(Sets.newHashSet());
	}	
	
}
