package com.pmease.gitplex.web.editable.teamchoice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.web.component.teamchoice.TeamChoiceProvider;
import com.pmease.gitplex.web.component.teamchoice.TeamSingleChoice;
import com.pmease.gitplex.web.page.depot.DepotPage;

@SuppressWarnings("serial")
public class TeamSingleChoiceEditor extends PropertyEditor<String> {
	
	private TeamSingleChoice input;
	
	public TeamSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
    	TeamChoiceProvider teamProvider = new TeamChoiceProvider(new LoadableDetachableModel<EntityCriteria<Team>>() {

			@Override
			protected EntityCriteria<Team> load() {
				EntityCriteria<Team> criteria = EntityCriteria.of(Team.class);
				criteria.add(Restrictions.eq("owner", getOwner()));
				return criteria;
			}
    		
    	});

    	Team team;
		if (getModelObject() != null)
			team =  GitPlex.getInstance(TeamManager.class).findBy(getOwner(), getModelObject());
		else
			team = null;
    	input = new TeamSingleChoice("input", Model.of(team), teamProvider, !getPropertyDescriptor().isPropertyRequired());
        input.setConvertEmptyInputStringToNull(true);
        
        add(input);
	}
	
	private User getOwner() {
		return ((DepotPage)getPage()).getDepot().getOwner();
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
