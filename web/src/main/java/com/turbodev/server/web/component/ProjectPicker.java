package com.turbodev.server.web.component;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.turbodev.server.TurboDev;
import com.turbodev.server.model.Project;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.web.component.floating.FloatingPanel;
import com.turbodev.server.web.component.link.DropdownLink;
import com.turbodev.server.web.component.projectselector.ProjectSelector;

@SuppressWarnings("serial")
public abstract class ProjectPicker extends DropdownLink {

	private final IModel<Collection<Project>> projectsModel; 
	
	private Long currentProjectId;
	
	public ProjectPicker(String id, IModel<Collection<Project>> projectsModel, Long currentProjectId) {
		super(id);
	
		this.projectsModel = projectsModel;
		this.currentProjectId = currentProjectId;
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new ProjectSelector(id, projectsModel, currentProjectId) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Project project) {
				dropdown.close();
				target.add(ProjectPicker.this);
				ProjectPicker.this.onSelect(target, project);
			}

		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setEscapeModelStrings(false);
	}

	@Override
	public IModel<?> getBody() {
		Project currentProject = TurboDev.getInstance(Dao.class).load(Project.class, currentProjectId);
		return Model.of(String.format("<i class='fa fa-ext fa-repo'></i> <span>%s</span> <i class='fa fa-caret-down'></i>", 
				currentProject.getName()));
	}

	@Override
	protected void onDetach() {
		projectsModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, Project project);
}
