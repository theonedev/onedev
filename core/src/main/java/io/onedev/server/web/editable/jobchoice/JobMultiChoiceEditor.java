package io.onedev.server.web.editable.jobchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.web.component.jobchoice.JobMultiChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathElement;
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
		
    	List<String> jobNames;
		if (getModelObject() != null)
			jobNames = getModelObject();
		else
			jobNames = new ArrayList<>();
		
		input = new JobMultiChoice("input", Model.of(jobNames));
        input.setConvertEmptyInputStringToNull(true);
        input.setLabel(Model.of(getDescriptor().getDisplayName(this)));
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathElement element) {
		return null;
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
