package com.pmease.gitplex.web.editable.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.pmease.gitplex.core.GitPlex;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.user.UserMultiChoice;

@SuppressWarnings("serial")
public class UserMultiChoiceEditor extends PropertyEditor<List<Long>> {
	
	private UserMultiChoice input;
	
	public UserMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Long>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	List<User> useres = new ArrayList<>();
		if (getModelObject() != null) {
			Dao dao = GitPlex.getInstance(Dao.class);
			for (Long userId: getModelObject()) 
				useres.add(dao.load(User.class, userId));
		} 
		
		input = new UserMultiChoice("input", new Model((Serializable)useres));
        input.setConvertEmptyInputStringToNull(true);
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected List<Long> convertInputToValue() throws ConversionException {
		List<Long> userIds = new ArrayList<>();
		Collection<User> useres = input.getConvertedInput();
		if (useres != null) {
			for (User user: useres)
				userIds.add(user.getId());
		} 
		return userIds;
	}

}
