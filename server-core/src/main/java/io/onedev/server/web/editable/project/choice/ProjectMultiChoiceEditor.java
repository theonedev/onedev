package io.onedev.server.web.editable.project.choice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.web.component.project.choice.ProjectMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.ProjectChoice;

@SuppressWarnings("serial")
public class ProjectMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private final IModel<List<Project>> choicesModel = new LoadableDetachableModel<List<Project>>() {

		@SuppressWarnings("unchecked")
		@Override
		protected List<Project> load() {
			ProjectChoice projectChoice = descriptor.getPropertyGetter().getAnnotation(ProjectChoice.class);
			if (projectChoice.value().length() != 0) {
				return (List<Project>) ReflectionUtils.invokeStaticMethod(
						descriptor.getPropertyGetter().getDeclaringClass(), projectChoice.value());
			} else {
				ProjectCache cache = getProjectManager().cloneCache();
				List<Project> projects = new ArrayList<>(cache.getProjects());
				projects.sort(cache.comparingPath());
				return projects;
			}
		}
		
	};
	
	private ProjectMultiChoice input;
	
	public ProjectMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onDetach() {
		choicesModel.detach();
		super.onDetach();
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<Project> selections = new ArrayList<>();
		if (getModelObject() != null) {
			for (String projectPath: getModelObject()) {
				Project selection = getProjectManager().findByPath(projectPath);
				if (selection != null) 
					selections.add(selection);
			}
		} 
		
		input = new ProjectMultiChoice("input", Model.of(selections), choicesModel) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
			
		};
        input.setLabel(Model.of(getDescriptor().getDisplayName()));
        
		input.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		
        add(input);
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> projectPaths = new ArrayList<>();
		Collection<Project> projects = input.getConvertedInput();
		if (projects != null) {
			for (Project project: projects)
				projectPaths.add(project.getPath());
		} 
		return projectPaths;
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
