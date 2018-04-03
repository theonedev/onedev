package io.onedev.server.web.editable.branchpattern;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.BranchPatternAssistBehavior;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public class BranchPatternEditor extends PropertyEditor<String> {
	
	private TextField<String> input;
	
	public BranchPatternEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
    	input = new TextField<String>("input", getModel());
    	input.add(new BranchPatternAssistBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return ((ProjectPage)getPage()).getProject();
			}
    		
    	}));
		input.setLabel(Model.of(getPropertyDescriptor().getDisplayName(this)));
        
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
