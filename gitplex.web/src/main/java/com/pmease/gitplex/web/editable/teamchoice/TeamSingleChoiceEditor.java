package com.pmease.gitplex.web.editable.teamchoice;

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
import com.pmease.gitplex.web.component.teamchoice.TeamSingleChoice;
import com.pmease.gitplex.web.page.account.AccountPage;

@SuppressWarnings("serial")
public class TeamSingleChoiceEditor extends PropertyEditor<String> {
	
	private TeamSingleChoice input;
	
	public TeamSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
    	TeamChoiceProvider teamProvider = new TeamChoiceProvider(new AbstractReadOnlyModel<Account>() {

			@Override
			public Account getObject() {
				return ((AccountPage)getPage()).getAccount();
			}
    		
    	});

    	String team = getModelObject();
    	input = new TeamSingleChoice("input", Model.of(team), teamProvider, !getPropertyDescriptor().isPropertyRequired());
        input.setConvertEmptyInputStringToNull(true);
        
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
