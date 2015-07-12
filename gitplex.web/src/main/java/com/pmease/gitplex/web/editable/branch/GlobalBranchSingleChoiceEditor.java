package com.pmease.gitplex.web.editable.branch;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.web.component.branch.GlobalBranchSingleChoice;

@SuppressWarnings("serial")
public class GlobalBranchSingleChoiceEditor extends PropertyEditor<RepoAndBranch> {
	
	private GlobalBranchSingleChoice input;
	
	public GlobalBranchSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<RepoAndBranch> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
		input = new GlobalBranchSingleChoice("input", getModel());
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected RepoAndBranch convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
