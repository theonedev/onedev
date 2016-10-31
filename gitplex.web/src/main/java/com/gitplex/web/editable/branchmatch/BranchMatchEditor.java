package com.gitplex.web.editable.branchmatch;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.core.entity.Depot;
import com.gitplex.web.component.branchmatch.BranchMatchInput;
import com.gitplex.web.page.depot.DepotPage;
import com.gitplex.commons.wicket.editable.ErrorContext;
import com.gitplex.commons.wicket.editable.PathSegment;
import com.gitplex.commons.wicket.editable.PropertyDescriptor;
import com.gitplex.commons.wicket.editable.PropertyEditor;

@SuppressWarnings("serial")
public class BranchMatchEditor extends PropertyEditor<String> {
	
	private BranchMatchInput input;
	
	public BranchMatchEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
    	input = new BranchMatchInput("input", new AbstractReadOnlyModel<Depot>() {

			@Override
			public Depot getObject() {
				return ((DepotPage) getPage()).getDepot();
			}
    		
    	}, getModel());
        
        add(input);
        
		add(new AttributeAppender("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (hasErrors(true))
					return " has-error";
				else
					return "";
			}
			
		}));
        
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
