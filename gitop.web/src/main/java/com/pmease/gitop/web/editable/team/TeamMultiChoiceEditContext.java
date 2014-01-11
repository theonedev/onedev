package com.pmease.gitop.web.editable.team;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.eclipse.jgit.util.StringUtils;

import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;

@SuppressWarnings("serial")
public class TeamMultiChoiceEditContext extends PropertyEditContext {

    public TeamMultiChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Object renderForEdit(Object renderParam) {
		return new TeamMultiChoiceEditor((String) renderParam, this);
    }

    @SuppressWarnings("unchecked")
	@Override
    public Object renderForView(Object renderParam) {
        Collection<Long> teamIds = (Collection<Long>) getPropertyValue();
        if (teamIds != null && !teamIds.isEmpty()) {
        	TeamManager teamManager = Gitop.getInstance(TeamManager.class);
        	List<String> teamNames = new ArrayList<>();
        	for (Long teamId: teamIds) {
        		teamNames.add(teamManager.load(teamId).getName());
        	}
            return new Label((String) renderParam, StringUtils.join(teamNames, ", " ));
        } else {
            return new Label((String) renderParam, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
