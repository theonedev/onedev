package io.onedev.server.web.editable.groupchoice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.web.component.groupchoice.GroupChoiceProvider;
import io.onedev.server.web.component.groupchoice.GroupSingleChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

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
			group = OneDev.getInstance(GroupManager.class).find(getModelObject());
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
