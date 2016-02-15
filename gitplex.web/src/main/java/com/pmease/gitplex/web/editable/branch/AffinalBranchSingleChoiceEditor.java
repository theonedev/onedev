package com.pmease.gitplex.web.editable.branch;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.web.component.branchchoice.AffinalBranchSingleChoice;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class AffinalBranchSingleChoiceEditor extends PropertyEditor<String> {
	
	private AffinalBranchSingleChoice input;
	
	public AffinalBranchSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
		Long repoId = ((RepositoryPage)getPage()).getDepot().getId();
		input = new AffinalBranchSingleChoice("input", repoId, getModel(), 
				!getPropertyDescriptor().isPropertyRequired());
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
