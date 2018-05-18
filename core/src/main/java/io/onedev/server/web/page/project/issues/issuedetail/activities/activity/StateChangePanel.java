package io.onedev.server.web.page.project.issues.issuedetail.activities.activity;

import java.util.List;

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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.changedata.StateChangeData;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.comment.ProjectAttachmentSupport;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
public abstract class StateChangePanel extends Panel {

	private static final String COMMENT_ID = "comment";
	
	public StateChangePanel(String id) {
		super(id);
	}
	
	private Component newViewer() {
		Fragment viewer = new Fragment(COMMENT_ID, "viewFrag", this);
		
		if (StringUtils.isNotBlank(getChangeData().getComment())) {
			viewer.add(new MarkdownViewer("content", new IModel<String>() {

				@Override
				public String getObject() {
					return getChangeData().getComment();
				}

				@Override
				public void detach() {
				}

				@Override
				public void setObject(String object) {
					getChangeData().setComment(object);
					getChange().setData(getChangeData());
					OneDev.getInstance(IssueChangeManager.class).save(getChange());				
				}

			}, null));
		} else {
			viewer.add(new Label("content", "<i>No comment</i>").setEscapeModelStrings(false));
		}
		
		WebMarkupContainer actions = new WebMarkupContainer("actions");
		actions.setVisible(SecurityUtils.canModify(getIssue()));
		actions.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment editor = new Fragment(COMMENT_ID, "editFrag", StateChangePanel.this);
				
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				editor.add(form);
				
				CommentInput input = new CommentInput("input", Model.of(getChangeData().getComment()), false) {

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
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						getChangeData().setComment(input.getModelObject());
						getChange().setData(getChangeData());
						OneDev.getInstance(IssueChangeManager.class).save(getChange());

						Component viewer = newViewer();
						editor.replaceWith(viewer);
						target.add(viewer);
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
		
		List<String> oldLines = getChangeData().getLines(getChangeData().getOldFields());
		List<String> newLines = getChangeData().getLines(getChangeData().getNewFields());
		add(new PlainDiffPanel("fields", oldLines, newLines).setVisible(!oldLines.equals(newLines)));
		
		add(newViewer());
	}

	private Issue getIssue() {
		return getChange().getIssue();
	}
	
	protected abstract StateChangeData getChangeData();
	
	protected abstract IssueChange getChange();
	
}
