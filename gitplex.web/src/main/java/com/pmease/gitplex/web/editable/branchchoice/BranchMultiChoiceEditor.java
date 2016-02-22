package com.pmease.gitplex.web.editable.branchchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.web.component.branchchoice.BranchChoiceProvider;
import com.pmease.gitplex.web.component.branchchoice.BranchMultiChoice;
import com.pmease.gitplex.web.page.depot.DepotPage;

@SuppressWarnings("serial")
public class BranchMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private BranchMultiChoice input;
	
	public BranchMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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

    	ArrayList<String> depotAndBranches = new ArrayList<>();
		if (getModelObject() != null) 
			depotAndBranches.addAll(getModelObject());
		
		input = new BranchMultiChoice("input", new Model(depotAndBranches), branchProvider);
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> depotAndBranches = new ArrayList<>();
		Collection<String> convertedInput = input.getConvertedInput();
		if (convertedInput != null) 
			depotAndBranches.addAll(convertedInput);
		return depotAndBranches;
	}

}
