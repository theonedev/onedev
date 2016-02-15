package com.pmease.gitplex.web.editable.team;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.web.component.teamchoice.TeamChoiceProvider;
import com.pmease.gitplex.web.component.teamchoice.TeamMultiChoice;
import com.pmease.gitplex.web.page.depot.DepotPage;

@SuppressWarnings("serial")
public class TeamMultiChoiceEditor extends PropertyEditor<List<Long>> {
	
	private TeamMultiChoice input;
	
	public TeamMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Long>> propertyModel) {
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
				DepotPage page = (DepotPage) getPage();
				criteria.add(Restrictions.eq("owner", page.getDepot().getOwner()));
				return criteria;
			}
    		
    	});

    	List<Team> teames = new ArrayList<>();
		if (getModelObject() != null) {
			Dao dao = GitPlex.getInstance(Dao.class);
			for (Long teamId: getModelObject()) 
				teames.add(dao.load(Team.class, teamId));
		} 
		
		input = new TeamMultiChoice("input", new Model((Serializable)teames), teamProvider);
        input.setConvertEmptyInputStringToNull(true);
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected List<Long> convertInputToValue() throws ConversionException {
		List<Long> teamIds = new ArrayList<>();
		Collection<Team> teames = input.getConvertedInput();
		if (teames != null) {
			for (Team team: teames)
				teamIds.add(team.getId());
		}
		return teamIds;
	}

}
