package io.onedev.server.web.component.project;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.model.Project;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.project.selector.ProjectSelector;
import io.onedev.server.web.component.svg.SpriteImage;

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
		return Model.of(String.format(""
				+ "<span class='project-picker'>"
				+ "  <svg class='icon'><use xlink:href='%s'/></svg>"
				+ "  <span>%s</span>"
				+ "  <svg class='icon rotate-90'><use xlink:href='%s'/></svg>"
				+ "</span>", 
				SpriteImage.getVersionedHref(IconScope.class, "project"),
				HtmlEscape.escapeHtml5(getCurrent().getName()), 
				SpriteImage.getVersionedHref(IconScope.class, "arrow")));
	}

	@Override
	protected void onDetach() {
		projectsModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, Project project);
	
	protected abstract Project getCurrent();
	
}
