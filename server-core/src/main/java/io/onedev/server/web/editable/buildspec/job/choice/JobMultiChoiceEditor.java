package io.onedev.server.web.editable.buildspec.job.choice;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.annotation.JobChoice;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.job.JobMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

public class JobMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private JobMultiChoice input;
	
	public JobMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<String> choices = new ArrayList<>();
		for (String jobName: Project.get().getJobNames())
			choices.add(jobName);
		
		JobChoice jobChoice = Preconditions.checkNotNull(descriptor.getPropertyGetter().getAnnotation(JobChoice.class));
    	List<String> selections = new ArrayList<>();
		if (getModelObject() != null) {
			for (String selection: getModelObject()) {
				if (jobChoice.tagsMode() || choices.contains(selection))
					selections.add(selection);
			}
		}
		
		input = new JobMultiChoice("input", Model.of(selections), Model.ofList(choices), jobChoice.tagsMode()) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
			
		};
		input.setRequired(descriptor.isPropertyRequired());
		
        input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
        
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

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
