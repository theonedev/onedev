package com.pmease.gitop.web.editable.team;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.eclipse.jgit.util.StringUtils;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.PropertyEditContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Team;

@SuppressWarnings("serial")
public class TeamMultiChoiceEditContext extends PropertyEditContext {

    public TeamMultiChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Component renderForEdit(String componentId) {
		return new TeamMultiChoiceEditor(componentId, this);
    }

    @SuppressWarnings("unchecked")
	@Override
    public Component renderForView(String componentId) {
        Collection<Long> teamIds = (Collection<Long>) getPropertyValue();
        if (teamIds != null && !teamIds.isEmpty()) {
        	Dao dao = Gitop.getInstance(Dao.class);
        	List<String> teamNames = new ArrayList<>();
        	for (Long teamId: teamIds) {
        		teamNames.add(dao.load(Team.class, teamId).getName());
        	}
            return new Label(componentId, StringUtils.join(teamNames, ", " ));
        } else {
            return new Label(componentId, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
