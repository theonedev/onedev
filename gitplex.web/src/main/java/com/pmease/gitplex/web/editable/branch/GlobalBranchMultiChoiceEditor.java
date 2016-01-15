package com.pmease.gitplex.web.editable.branch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.web.component.branchchoice.GlobalBranchMultiChoice;

@SuppressWarnings("serial")
public class GlobalBranchMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private GlobalBranchMultiChoice input;
	
	public GlobalBranchMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new GlobalBranchMultiChoice("input", new LoadableDetachableModel<Collection<String>>() {

			@Override
			protected Collection<String> load() {
		    	List<String> branches = new ArrayList<>();
				if (getModelObject() != null)
					branches.addAll(getModelObject());
				
				return branches;
			}
    		
    	});
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> repoAndBranches = new ArrayList<>();
		Collection<String> convertedInput = input.getConvertedInput();
		if (convertedInput != null)
			repoAndBranches.addAll(convertedInput);
		return repoAndBranches;
	}

}
