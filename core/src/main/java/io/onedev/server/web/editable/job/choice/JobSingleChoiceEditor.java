package io.onedev.server.web.editable.job.choice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.web.component.job.JobSingleChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class JobSingleChoiceEditor extends PropertyEditor<String> {
	
	private JobSingleChoice input;
	
	public JobSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new JobSingleChoice("input", Model.of(getModelObject()));
        input.setConvertEmptyInputStringToNull(true);
        input.setLabel(Model.of(getDescriptor().getDisplayName(this)));
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathElement element) {
		return null;
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
