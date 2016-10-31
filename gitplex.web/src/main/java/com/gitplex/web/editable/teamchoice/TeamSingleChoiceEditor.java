package com.gitplex.web.editable.teamchoice;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Team;
import com.gitplex.core.manager.TeamManager;
import com.gitplex.web.component.teamchoice.TeamChoiceProvider;
import com.gitplex.web.component.teamchoice.TeamSingleChoice;
import com.gitplex.web.page.account.AccountPage;
import com.gitplex.commons.wicket.editable.ErrorContext;
import com.gitplex.commons.wicket.editable.PathSegment;
import com.gitplex.commons.wicket.editable.PropertyDescriptor;
import com.gitplex.commons.wicket.editable.PropertyEditor;

@SuppressWarnings("serial")
public class TeamSingleChoiceEditor extends PropertyEditor<String> {
	
	private TeamSingleChoice input;
	
	public TeamSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		IModel<Account> organizationModel = new AbstractReadOnlyModel<Account>() {

			@Override
			public Account getObject() {
				return ((AccountPage)getPage()).getAccount();
			}
			
		};
		Team team;
		if (getModelObject() != null)
			team = GitPlex.getInstance(TeamManager.class).find(organizationModel.getObject(), getModelObject());
		else
			team = null;
		
    	input = new TeamSingleChoice("input", Model.of(team), new TeamChoiceProvider(organizationModel));
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
		Team team = input.getConvertedInput();
		if (team != null)
			return team.getName();
		else
			return null;
	}

}
