package com.turbodev.server.web.editable.reviewrequirementspec;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import com.turbodev.server.model.Project;
import com.turbodev.server.web.component.reviewrequirementspec.ReviewRequirementSpecInput;
import com.turbodev.server.web.editable.ErrorContext;
import com.turbodev.server.web.editable.PathSegment;
import com.turbodev.server.web.editable.PropertyDescriptor;
import com.turbodev.server.web.editable.PropertyEditor;
import com.turbodev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public class ReviewRequirementSpecEditor extends PropertyEditor<String> {
	
	private ReviewRequirementSpecInput input;
	
	public ReviewRequirementSpecEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
    	input = new ReviewRequirementSpecInput("input", new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return ((ProjectPage) getPage()).getProject();
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
