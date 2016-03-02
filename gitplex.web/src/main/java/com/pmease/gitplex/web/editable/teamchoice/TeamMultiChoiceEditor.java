package com.pmease.gitplex.web.editable.teamchoice;

import java.io.Serializable;
import java.util.Collection;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.web.component.teamchoice.TeamChoiceProvider;
import com.pmease.gitplex.web.component.teamchoice.TeamMultiChoice;
import com.pmease.gitplex.web.page.account.AccountPage;

@SuppressWarnings("serial")
public class TeamMultiChoiceEditor extends PropertyEditor<Collection<String>> {
	
	private TeamMultiChoice input;
	
	public TeamMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Collection<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	TeamChoiceProvider teamProvider = new TeamChoiceProvider(new AbstractReadOnlyModel<Account>() {

			@Override
			public Account getObject() {
				return ((AccountPage)getPage()).getAccount();
			}
			
    	});

		input = new TeamMultiChoice("input", new Model((Serializable)getModelObject()), teamProvider);
        input.setConvertEmptyInputStringToNull(true);
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected Collection<String> convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
