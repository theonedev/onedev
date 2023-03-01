package io.onedev.server.web.editable.buildspec.job.choice;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.model.Project;
import io.onedev.server.web.component.job.JobSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.annotation.JobChoice;

@SuppressWarnings("serial")
public class JobSingleChoiceEditor extends PropertyEditor<String> {
	
	private JobSingleChoice input;
	
	public JobSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Map<String, String> choices = new LinkedHashMap<>();
		for (String jobName: Project.get().getJobNames())
			choices.put(jobName, jobName);
		
		JobChoice jobChoice = Preconditions.checkNotNull(descriptor.getPropertyGetter().getAnnotation(JobChoice.class));
		
		String selection = getModelObject();
		if (!jobChoice.tagsMode() && !choices.containsKey(selection))
			selection = null;
		
		input = new JobSingleChoice("input", Model.of(selection), Model.ofMap(choices), jobChoice.tagsMode()) {
			
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
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
