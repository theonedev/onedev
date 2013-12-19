package com.pmease.gitop.web.page.project.source.tree;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.page.project.source.component.ProjectPanel;

public class ProjectDescriptionPanel extends ProjectPanel {
	private static final long serialVersionUID = 1L;
	
	static enum Mode {
		LABEL, EDITOR
	}
	
	private Mode mode = Mode.LABEL;
	
	public ProjectDescriptionPanel(String id, IModel<Project> model) {
		super(id, model);
		
		setOutputMarkupId(true);
	}
	
	@SuppressWarnings("serial")
	private Component newContent(String id) {
		if (mode == Mode.LABEL) {
			Fragment frag = new Fragment(id, "label", ProjectDescriptionPanel.this);
			frag.add(new Label("description", new PropertyModel<String>(getDefaultModel(), "description")));
			frag.add(new AjaxLink<Void>("editlink") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					mode = Mode.EDITOR;
					onModeChanged(target);
				}
			});
			
			return frag;
		} else {
			Fragment frag = new Fragment(id, "editor", ProjectDescriptionPanel.this);
			Form<?> form = new Form<Void>("form");
			form.add(new TextField<String>("input", new PropertyModel<String>(ProjectDescriptionPanel.this.getDefaultModel(), "description")));
			form.add(new AjaxButton("save", form) {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					Project project = getProject();
					Gitop.getInstance(ProjectManager.class).save(project);
					mode = Mode.LABEL;
					onModeChanged(target);
				}
			});
			
			form.add(new AjaxLink<Void>("cancel") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					mode = Mode.LABEL;
					onModeChanged(target);
				}
				
			});
			
			frag.add(form);
			return frag;
		}
	}
	
	private void onModeChanged(AjaxRequestTarget target) {
		Component c = newContent("description");
		addOrReplace(c);
		target.add(this);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(newContent("description"));
	}
}
