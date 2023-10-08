package io.onedev.server.web.editable.build.choice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.build.choice.BuildChoiceProvider;
import io.onedev.server.web.component.build.choice.BuildSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.util.ProjectAware;

@SuppressWarnings("serial")
public class BuildSingleChoiceEditor extends PropertyEditor<Long> {

	private BuildSingleChoice input;
	
	public BuildSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<Long> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	private Project getProject() {
		ProjectAware projectAware = findParent(ProjectAware.class);
		if (projectAware != null)
			return projectAware.getProject();		
		else
			return null;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Build build;
		if (getModelObject() != null)
			build = OneDev.getInstance(BuildManager.class).get(getModelObject());
		else
			build = null;
		
		BuildChoiceProvider choiceProvider = new BuildChoiceProvider(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
    		
    	});
    	input = new BuildSingleChoice("input", Model.of(build), choiceProvider) {

    		@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
				getSettings().setAllowClear(!descriptor.isPropertyRequired());
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
	protected Long convertInputToValue() throws ConversionException {
		Build build = input.getConvertedInput();
		if (build != null)
			return build.getId();
		else
			return null;
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
