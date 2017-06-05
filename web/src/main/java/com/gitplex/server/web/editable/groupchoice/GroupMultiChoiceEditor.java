package com.gitplex.server.web.editable.groupchoice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.model.Group;
import com.gitplex.server.util.facade.GroupFacade;
import com.gitplex.server.web.component.groupchoice.GroupChoiceProvider;
import com.gitplex.server.web.component.groupchoice.GroupMultiChoice;
import com.gitplex.server.web.editable.ErrorContext;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class GroupMultiChoiceEditor extends PropertyEditor<Collection<String>> {
	
	private GroupMultiChoice input;
	
	public GroupMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Collection<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	List<GroupFacade> groups = new ArrayList<>();
		if (getModelObject() != null) {
			GroupManager groupManager = GitPlex.getInstance(GroupManager.class);
			for (String groupName: getModelObject()) {
				Group group = groupManager.find(groupName);
				if (group != null)
					groups.add(group.getFacade());
			}
		} 
		
		input = new GroupMultiChoice("input", new Model((Serializable)groups), new GroupChoiceProvider());
        input.setConvertEmptyInputStringToNull(true);
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> groupNames = new ArrayList<>();
		Collection<GroupFacade> groups = input.getConvertedInput();
		if (groups != null) {
			for (GroupFacade group: groups)
				groupNames.add(group.getName());
		} 
		return groupNames;
	}

}
