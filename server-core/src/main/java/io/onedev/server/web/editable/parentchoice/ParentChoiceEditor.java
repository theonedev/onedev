package io.onedev.server.web.editable.parentchoice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.security.permission.CreateChildren;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class ParentChoiceEditor extends PropertyEditor<String> {

	private StringSingleChoice input;
	
	public ParentChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		IModel<Map<String, String>> choicesModel = new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				List<String> listOfProjectPath = new ArrayList<>();
				Project currentProject = Project.get();
				
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
				ProjectCache cache = projectManager.cloneCache();
				for (Project project: projectManager.getPermittedProjects(new CreateChildren())) {
					if (currentProject == null || !cache.isSelfOrAncestorOf(currentProject.getId(), project.getId())) {
						String projectPath = cache.getPath(project.getId());
						listOfProjectPath.add(projectPath);
					}
				}
				Collections.sort(listOfProjectPath);
				
				Map<String, String> mapOfProjectPath = new LinkedHashMap<>();
				for (String projectPath: listOfProjectPath)
					mapOfProjectPath.put(projectPath, projectPath);
				return mapOfProjectPath;
			}
			
		};
		
		String selection = getModelObject();
		
    	input = new StringSingleChoice("input", Model.of(selection), choicesModel, true) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
				getSettings().setAllowClear(true);
			}
    		
    	};
    	input.setRequired(false);

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
		return input.getConvertedInput();
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
