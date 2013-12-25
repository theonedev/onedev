package com.pmease.gitop.web.editable.team;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Team;

@SuppressWarnings("serial")
public class TeamSingleChoiceEditContext extends PropertyEditContext {

    public TeamSingleChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Object renderForEdit(Object renderParam) {
		return new TeamSingleChoiceEditor((String) renderParam, this);
    }

    @Override
    public Object renderForView(Object renderParam) {
        Enum<?> propertyValue = (Enum<?>) getPropertyValue();
        if (propertyValue != null) {
        	Team team = Gitop.getInstance(TeamManager.class).load((Long) getPropertyValue());
            return new Label((String) renderParam, team.getName());
        } else {
            return new Label((String) renderParam, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
