package io.onedev.server.web.component.project;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.model.Project;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.project.selector.ProjectSelector;

@SuppressWarnings("serial")
public abstract class ProjectPicker extends DropdownLink {

	private final IModel<Collection<Project>> projectsModel; 
	
	public ProjectPicker(String id, IModel<Collection<Project>> projectsModel) {
		super(id);
	
		this.projectsModel = projectsModel;
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new ProjectSelector(id, projectsModel) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Project project) {
				dropdown.close();
				target.add(ProjectPicker.this);
				ProjectPicker.this.onSelect(target, project);
			}

			@Override
			protected Project getCurrent() {
				return ProjectPicker.this.getCurrent();
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
		return Model.of(String.format("<i class='fa fa-ext fa-repo'></i> <span>%s</span> <i class='fa fa-caret-down'></i>", 
				HtmlEscape.escapeHtml5(getCurrent().getName())));
	}

	@Override
	protected void onDetach() {
		projectsModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, Project project);
	
	protected abstract Project getCurrent();
	
}
