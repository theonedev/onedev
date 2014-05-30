package com.pmease.gitop.web.editable.team;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.PropertyEditContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Team;

@SuppressWarnings("serial")
public class TeamSingleChoiceEditContext extends PropertyEditContext {

    public TeamSingleChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Component renderForEdit(String componentId) {
		return new TeamSingleChoiceEditor(componentId, this);
    }

    @Override
    public Component renderForView(String componentId) {
        Long teamId = (Long) getPropertyValue();
        if (teamId != null) {
        	Team team = Gitop.getInstance(Dao.class).load(Team.class, teamId);
            return new Label(componentId, team.getName());
        } else {
            return new Label(componentId, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
