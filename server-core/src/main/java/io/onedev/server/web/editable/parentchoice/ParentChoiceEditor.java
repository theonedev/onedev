package io.onedev.server.web.editable.parentchoice;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.CreateChildren;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

public class ParentChoiceEditor extends PropertyEditor<String> {

	private StringSingleChoice input;
	
	public ParentChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		IModel<List<String>> choicesModel = new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				List<String> projectPaths = new ArrayList<>();
				Project currentProject = Project.get();
				
				ProjectService projectService = OneDev.getInstance(ProjectService.class);
				ProjectCache cache = projectService.cloneCache();
				for (Project project: SecurityUtils.getAuthorizedProjects(new CreateChildren())) {
					if (currentProject == null || !cache.isSelfOrAncestorOf(currentProject.getId(), project.getId())) {
						String projectPath = cache.get(project.getId()).getPath();
						projectPaths.add(projectPath);
					}
				}
				Collections.sort(projectPaths);				
				return projectPaths;
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

        input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
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
