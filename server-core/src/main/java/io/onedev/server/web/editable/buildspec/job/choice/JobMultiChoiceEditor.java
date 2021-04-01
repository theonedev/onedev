package io.onedev.server.web.editable.buildspec.job.choice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.model.Project;
import io.onedev.server.web.component.job.JobMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class JobMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private JobMultiChoice input;
	
	public JobMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Map<String, String> choices = new LinkedHashMap<>();
		for (String jobName: Project.get().getJobNames())
			choices.put(jobName, jobName);
		
    	List<String> selections = new ArrayList<>();
		if (getModelObject() != null) {
			for (String selection: getModelObject()) {
				if (choices.containsKey(selection))
					selections.add(selection);
			}
		}
		
		input = new JobMultiChoice("input", Model.of(selections), Model.ofMap(choices)) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
			
		};
		input.setRequired(descriptor.isPropertyRequired());
		
        input.setLabel(Model.of(getDescriptor().getDisplayName()));
        
        add(input);
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		Collection<String> convertedInput = input.getConvertedInput();
		if (convertedInput != null)
			return new ArrayList<>(convertedInput);
		else
			return null;
	}

}
