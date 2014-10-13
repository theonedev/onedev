package com.pmease.gitplex.web.editable.branch;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.web.component.branch.GlobalBranchSingleChoice;

@SuppressWarnings("serial")
public class GlobalBranchSingleChoiceEditor extends PropertyEditor<Long> {
	
	private GlobalBranchSingleChoice input;
	
	public GlobalBranchSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Long> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
    	Branch branch;
		if (getModelObject() != null)
			branch =  GitPlex.getInstance(Dao.class).load(Branch.class, getModelObject());
		else
			branch = null;

		input = new GlobalBranchSingleChoice("input", Model.of(branch));
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected Long convertInputToValue() throws ConversionException {
		Branch branch = input.getConvertedInput();
		if (branch != null)
			return branch.getId();
		else
			return null;
	}

}
