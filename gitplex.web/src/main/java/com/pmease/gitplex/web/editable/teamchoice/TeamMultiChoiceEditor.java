package com.pmease.gitplex.web.editable.teamchoice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.teamchoice.TeamChoiceProvider;
import com.pmease.gitplex.web.component.teamchoice.TeamMultiChoice;
import com.pmease.gitplex.web.page.depot.DepotPage;

@SuppressWarnings("serial")
public class TeamMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private TeamMultiChoice input;
	
	public TeamMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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

    	List<Team> teames = new ArrayList<>();
		if (getModelObject() != null) {
			TeamManager teamManager = GitPlex.getInstance(TeamManager.class);
			for (String teamName: getModelObject()) 
				teames.add(teamManager.findBy(getOwner(), teamName));
		} 
		
		input = new TeamMultiChoice("input", new Model((Serializable)teames), teamProvider);
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
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> teamNames = new ArrayList<>();
		Collection<Team> teames = input.getConvertedInput();
		if (teames != null) {
			for (Team team: teames)
				teamNames.add(team.getName());
		}
		return teamNames;
	}

}
