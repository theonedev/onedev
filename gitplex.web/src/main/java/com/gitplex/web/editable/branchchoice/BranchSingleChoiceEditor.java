package com.gitplex.web.editable.branchchoice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.core.entity.Depot;
import com.gitplex.web.component.branchchoice.BranchChoiceProvider;
import com.gitplex.web.component.branchchoice.BranchSingleChoice;
import com.gitplex.web.page.depot.DepotPage;
import com.gitplex.commons.wicket.editable.ErrorContext;
import com.gitplex.commons.wicket.editable.PathSegment;
import com.gitplex.commons.wicket.editable.PropertyDescriptor;
import com.gitplex.commons.wicket.editable.PropertyEditor;

@SuppressWarnings("serial")
public class BranchSingleChoiceEditor extends PropertyEditor<String> {
	
	private BranchSingleChoice input;
	
	public BranchSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
    	BranchChoiceProvider branchProvider = new BranchChoiceProvider(new LoadableDetachableModel<Depot>() {

			@Override
			protected Depot load() {
				DepotPage page = (DepotPage) getPage();
				return page.getDepot();
			}
    		
    	});

    	input = new BranchSingleChoice("input", getModel(), branchProvider);
    	input.setConvertEmptyInputStringToNull(true);
    	
        // add this to control allowClear flag of select2
    	input.setRequired(propertyDescriptor.isPropertyRequired());
        
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
