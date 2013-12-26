package com.pmease.gitop.web.editable.team;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.editable.TeamChoice;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.web.component.choice.TeamChoiceProvider;
import com.pmease.gitop.web.component.choice.TeamSingleChoice;
import com.pmease.gitop.web.page.project.AbstractProjectPage;

@SuppressWarnings("serial")
public class TeamSingleChoiceEditor extends Panel {
	
	private final TeamSingleChoiceEditContext editContext;

	public TeamSingleChoiceEditor(String id, TeamSingleChoiceEditContext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	IModel<Team> model = new IModel<Team>() {

			@Override
			public void detach() {
			}

			@Override
			public Team getObject() {
				Long teamId = (Long) editContext.getPropertyValue();
				if (teamId != null)
					return Gitop.getInstance(TeamManager.class).load(teamId); 
				else
					return null;
			}

			@Override
			public void setObject(Team object) {
				if (object != null)
					editContext.setPropertyValue(object.getId());
				else
					editContext.setPropertyValue(null);
			}
    		
    	};
    	
    	TeamChoiceProvider teamProvider = new TeamChoiceProvider(new LoadableDetachableModel<DetachedCriteria>() {

			@Override
			protected DetachedCriteria load() {
				DetachedCriteria criteria = DetachedCriteria.forClass(Team.class);
				AbstractProjectPage page = (AbstractProjectPage) getPage();
				criteria.add(Restrictions.eq("owner", page.getProject().getOwner()));
				for (String each: editContext.getPropertyGetter().getAnnotation(TeamChoice.class).excludes()) {
					criteria.add(Restrictions.not(Restrictions.eq("name", each)));
				}
				return criteria;
			}
    		
    	});

    	TeamSingleChoice chooser = new TeamSingleChoice("chooser", model, teamProvider);
        chooser.setConvertEmptyInputStringToNull(true);
        
        add(chooser);
	}

}
