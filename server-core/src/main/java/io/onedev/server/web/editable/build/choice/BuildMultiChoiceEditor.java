package io.onedev.server.web.editable.build.choice;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.service.BuildService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.build.choice.BuildChoiceProvider;
import io.onedev.server.web.component.build.choice.BuildMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.util.ProjectAware;

public class BuildMultiChoiceEditor extends PropertyEditor<List<Long>> {

	private BuildMultiChoice input;
	
	public BuildMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<List<Long>> propertyModel) {
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

		List<Build> selections = new ArrayList<>();
		if (getModelObject() != null) {
			BuildService buildService = OneDev.getInstance(BuildService.class);
			for (Long buildId: getModelObject()) {
				Build build = buildService.get(buildId);
				if (build != null)
					selections.add(build);
			}
		} 
		
		BuildChoiceProvider choiceProvider = new BuildChoiceProvider() {

			@Override
			protected Project getProject() {
				return BuildMultiChoiceEditor.this.getProject();
			}
		};
    	input = new BuildMultiChoice("input", Model.of(selections), choiceProvider) {

    		@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
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
	protected List<Long> convertInputToValue() throws ConversionException {
		Collection<Build> builds = input.getConvertedInput();
		if (builds != null)
			return builds.stream().map(it->it.getId()).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
