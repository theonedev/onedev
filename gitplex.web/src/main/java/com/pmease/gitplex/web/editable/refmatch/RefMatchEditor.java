package com.pmease.gitplex.web.editable.refmatch;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.web.component.refmatch.RefMatchInput;
import com.pmease.gitplex.web.page.depot.DepotPage;

@SuppressWarnings("serial")
public class RefMatchEditor extends PropertyEditor<String> {
	
	private RefMatchInput input;
	
	public RefMatchEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
    	input = new RefMatchInput("input", new AbstractReadOnlyModel<Depot>() {

			@Override
			public Depot getObject() {
				return ((DepotPage) getPage()).getDepot();
			}
    		
    	}, getModel());
        
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
