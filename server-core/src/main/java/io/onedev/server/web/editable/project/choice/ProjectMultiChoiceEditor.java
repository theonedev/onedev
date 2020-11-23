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
import io.onedev.server.web.component.project.choice.ProjectChoiceProvider;
import io.onedev.server.web.component.project.choice.ProjectMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class ProjectMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private final IModel<Collection<Project>> choicesModel = new LoadableDetachableModel<Collection<Project>>() {

		@Override
		protected Collection<Project> load() {
			return OneDev.getInstance(ProjectManager.class).query();
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

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<Project> selections = new ArrayList<>();
		if (getModelObject() != null) {
			ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
			for (String projectName: getModelObject()) {
				Project project = projectManager.find(projectName);
				if (project != null && choicesModel.getObject().contains(project))
					selections.add(project);
			}
		} 
		
		input = new ProjectMultiChoice("input", Model.of(selections), new ProjectChoiceProvider(choicesModel)) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
			
		};
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
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> projectNames = new ArrayList<>();
		Collection<Project> projects = input.getConvertedInput();
		if (projects != null) {
			for (Project project: projects)
				projectNames.add(project.getName());
		} 
		return projectNames;
	}

}
