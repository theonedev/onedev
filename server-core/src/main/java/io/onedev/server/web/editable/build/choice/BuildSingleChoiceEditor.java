package io.onedev.server.web.editable.build.choice;

import static io.onedev.server.web.translation.Translation._T;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.service.BuildService;
import io.onedev.server.web.component.build.choice.BuildChoiceProvider;
import io.onedev.server.web.component.build.choice.BuildSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.util.ProjectAware;

public class BuildSingleChoiceEditor extends PropertyEditor<Long> {

	@Inject
	private BuildService buildService;

	private BuildSingleChoice input;

	private final boolean useNumber;
	
	public BuildSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<Long> propertyModel, boolean useNumber) {
		super(id, propertyDescriptor, propertyModel);
		this.useNumber = useNumber;
	}

	@Nullable
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

		Build build = null;
		var buildIdOrNumber = getModelObject();
		if (buildIdOrNumber != null) {
			if (useNumber) {
				Preconditions.checkState(getProject() != null);
				build = buildService.find(getProject(), buildIdOrNumber);
			} else {
				build = buildService.get(buildIdOrNumber);
			}
		}
		
		BuildChoiceProvider choiceProvider = new BuildChoiceProvider(useNumber) {

			@Override
			protected Project getProject() {
				return BuildSingleChoiceEditor.this.getProject();
			}
		};
    	input = new BuildSingleChoice("input", Model.of(build), choiceProvider) {

    		@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
				getSettings().setAllowClear(!descriptor.isPropertyRequired());
			}
    		
    	};
        
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
	protected Long convertInputToValue() throws ConversionException {
		Build build = input.getConvertedInput();
		if (build != null)
			return useNumber? build.getNumber() : build.getId();
		else
			return null;
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
