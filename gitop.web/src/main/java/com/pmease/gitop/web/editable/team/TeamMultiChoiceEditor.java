package com.pmease.gitop.web.editable.team;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.editable.TeamChoice;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.web.component.choice.TeamChoiceProvider;
import com.pmease.gitop.web.component.choice.TeamMultiChoice;
import com.pmease.gitop.web.page.repository.RepositoryBasePage;

@SuppressWarnings("serial")
public class TeamMultiChoiceEditor extends Panel {
	
	private final TeamMultiChoiceEditContext editContext;

	public TeamMultiChoiceEditor(String id, TeamMultiChoiceEditContext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	IModel<Collection<Team>> model = new IModel<Collection<Team>>() {

			@Override
			public void detach() {
			}

			@SuppressWarnings("unchecked")
			@Override
			public Collection<Team> getObject() {
				List<Long> teamIds = (List<Long>) editContext.getPropertyValue();
				if (teamIds != null) {
					Dao dao = Gitop.getInstance(Dao.class);
					Collection<Team> teams = new ArrayList<>();
					for (Long teamId: teamIds) 
						teams.add(dao.load(Team.class, teamId));
					return teams;
				} else {
					return null;
				}
			}

			@Override
			public void setObject(Collection<Team> teams) {
				if (teams != null) {
					List<Long> teamIds = new ArrayList<>();
					for (Team team: teams)
						teamIds.add(team.getId());
					editContext.setPropertyValue((Serializable) teamIds);
				} else {
					editContext.setPropertyValue(null);
				}
			}
    		
    	};
    	
    	TeamChoiceProvider teamProvider = new TeamChoiceProvider(new LoadableDetachableModel<EntityCriteria<Team>>() {

			@Override
			protected EntityCriteria<Team> load() {
				EntityCriteria<Team> criteria = EntityCriteria.of(Team.class);
				RepositoryBasePage page = (RepositoryBasePage) getPage();
				criteria.add(Restrictions.eq("owner", page.getRepository()));
				for (String each: editContext.getPropertyGetter().getAnnotation(TeamChoice.class).excludes()) {
					criteria.add(Restrictions.not(Restrictions.eq("name", each)));
				}
				return criteria;
			}
    		
    	});

    	TeamMultiChoice chooser = new TeamMultiChoice("chooser", model, teamProvider);
        chooser.setConvertEmptyInputStringToNull(true);
        
        add(chooser);
	}

}
