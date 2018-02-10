package com.turbodev.server.web.editable.groupchoice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.GroupManager;
import com.turbodev.server.model.Group;
import com.turbodev.server.util.facade.GroupFacade;
import com.turbodev.server.web.component.groupchoice.GroupChoiceProvider;
import com.turbodev.server.web.component.groupchoice.GroupSingleChoice;
import com.turbodev.server.web.editable.ErrorContext;
import com.turbodev.server.web.editable.PathSegment;
import com.turbodev.server.web.editable.PropertyDescriptor;
import com.turbodev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class GroupSingleChoiceEditor extends PropertyEditor<String> {
	
	private GroupSingleChoice input;
	
	public GroupSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Group group;
		if (getModelObject() != null)
			group = TurboDev.getInstance(GroupManager.class).find(getModelObject());
		else
			group = null;
		
		GroupFacade facade = group!=null?group.getFacade():null;
    	input = new GroupSingleChoice("input", Model.of(facade), new GroupChoiceProvider());
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
		GroupFacade group = input.getConvertedInput();
		if (group != null)
			return group.getName();
		else
			return null;
	}

}
