package com.gitplex.server.web.editable.refmatch;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.server.model.Depot;
import com.gitplex.server.web.component.refmatch.RefMatchInput;
import com.gitplex.server.web.editable.ErrorContext;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.PropertyEditor;
import com.gitplex.server.web.page.depot.DepotPage;

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
