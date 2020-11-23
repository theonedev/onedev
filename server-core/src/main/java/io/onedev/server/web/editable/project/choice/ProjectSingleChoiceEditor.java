package io.onedev.server.web.editable.project.choice;

import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.project.choice.ProjectChoiceProvider;
import io.onedev.server.web.component.project.choice.ProjectSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class ProjectSingleChoiceEditor extends PropertyEditor<String> {

	private ProjectSingleChoice input;
	
	public ProjectSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Project> projects = OneDev.getInstance(ProjectManager.class).query();
		projects.sort(Comparator.comparing(Project::getName));
		
		Project project;
		if (getModelObject() != null)
			project = OneDev.getInstance(ProjectManager.class).find(getModelObject());
		else
			project = null;
		
		Project selection;
		if (project != null && projects.contains(project))
			selection = project;
		else
			selection = null;
		
    	input = new ProjectSingleChoice("input", Model.of(selection), new ProjectChoiceProvider(Model.of(projects))) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
    		
    	};
        
        // add this to control allowClear flag of select2
    	input.setRequired(descriptor.isPropertyRequired());
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
	protected String convertInputToValue() throws ConversionException {
		Project project = input.getConvertedInput();
		if (project != null)
			return project.getName();
		else
			return null;
	}

}
