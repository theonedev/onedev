package io.onedev.server.web.editable.build.choice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.build.choice.BuildChoiceProvider;
import io.onedev.server.web.component.build.choice.BuildMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public class BuildMultiChoiceEditor extends PropertyEditor<List<Long>> {

	private BuildMultiChoice input;
	
	public BuildMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<List<Long>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	private Project getProject() {
		return ((ProjectPage)getPage()).getProject();		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Build> selections = new ArrayList<>();
		if (getModelObject() != null) {
			BuildManager buildManager = OneDev.getInstance(BuildManager.class);
			for (Long buildNumber: getModelObject()) {
				Build build = buildManager.find(getProject(), buildNumber);
				if (build != null)
					selections.add(build);
			}
		} 
		
		BuildChoiceProvider choiceProvider = new BuildChoiceProvider(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
    		
    	});
    	input = new BuildMultiChoice("input", Model.of(selections), choiceProvider) {

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
	protected List<Long> convertInputToValue() throws ConversionException {
		Collection<Build> builds = input.getConvertedInput();
		if (builds != null)
			return builds.stream().map(it->it.getNumber()).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

}
