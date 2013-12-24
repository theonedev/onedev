package com.pmease.gitop.web.editable.team;

import java.io.Serializable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.web.component.choice.TeamChoiceProvider;
import com.pmease.gitop.web.component.choice.TeamSingleChoice;

@SuppressWarnings("serial")
public class TeamChoiceEditContext extends PropertyEditContext {

    public TeamChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Object renderForEdit(Object renderParam) {
    	IModel<Team> model = new IModel<Team>() {

			@Override
			public void detach() {
			}

			@Override
			public Team getObject() {
				Long teamId = (Long) getPropertyValue();
				if (teamId != null)
					return Gitop.getInstance(TeamManager.class).load(teamId); 
				else
					return null;
			}

			@Override
			public void setObject(Team object) {
				if (object != null)
					setPropertyValue(object.getId());
				else
					setPropertyValue(null);
			}
    		
    	};
        TeamSingleChoice chooser = new TeamSingleChoice(
        		(String)renderParam, model, new TeamChoiceProvider(null)) {
        	
            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.setName("input");
                tag.put("type", "hidden");
                super.onComponentTag(tag);
            }

        };
        chooser.setRequired(EditableUtils.isPropertyRequired(getPropertyGetter()));
        chooser.setConvertEmptyInputStringToNull(true);

        return chooser;
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
