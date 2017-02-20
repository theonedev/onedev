package com.gitplex.server.web.editable.branchchoice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.server.model.Depot;
import com.gitplex.server.web.component.branchchoice.BranchChoiceProvider;
import com.gitplex.server.web.component.branchchoice.BranchSingleChoice;
import com.gitplex.server.web.editable.ErrorContext;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.PropertyEditor;
import com.gitplex.server.web.page.depot.DepotPage;

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
