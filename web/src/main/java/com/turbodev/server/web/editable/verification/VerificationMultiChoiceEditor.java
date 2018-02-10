package com.turbodev.server.web.editable.verification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.turbodev.server.web.component.verification.VerificationChoiceProvider;
import com.turbodev.server.web.component.verification.VerificationMultiChoice;
import com.turbodev.server.web.editable.ErrorContext;
import com.turbodev.server.web.editable.PathSegment;
import com.turbodev.server.web.editable.PropertyDescriptor;
import com.turbodev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class VerificationMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private VerificationMultiChoice input;
	
	public VerificationMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	List<String> verifications = new ArrayList<>();
		if (getModelObject() != null) {
			verifications.addAll(getModelObject());
		} 
		
		input = new VerificationMultiChoice("input", new Model((Serializable) verifications), 
				new VerificationChoiceProvider());
        input.setConvertEmptyInputStringToNull(true);
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> verifications = new ArrayList<>();
		Collection<String> convertedInput = input.getConvertedInput();
		if (convertedInput != null) {
			for (String each: convertedInput)
				verifications.add(each);
		} 
		return verifications;
	}

}
