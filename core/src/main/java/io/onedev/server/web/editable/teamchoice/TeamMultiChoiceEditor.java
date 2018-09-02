package io.onedev.server.web.editable.teamchoice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.TeamManager;
import io.onedev.server.model.Team;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.facade.TeamFacade;
import io.onedev.server.web.component.teamchoice.TeamChoiceProvider;
import io.onedev.server.web.component.teamchoice.TeamMultiChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.TeamChoice;
import io.onedev.server.web.util.ComponentContext;
import io.onedev.utils.ReflectionUtils;

@SuppressWarnings("serial")
public class TeamMultiChoiceEditor extends PropertyEditor<Collection<String>> {
	
	private final List<TeamFacade> choices = new ArrayList<>();
	
	private TeamMultiChoice input;
	
	public TeamMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Collection<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		OneContext oneContext = new ComponentContext(this);
		
    	List<TeamFacade> teams = new ArrayList<>();
		OneContext.push(oneContext);
		try {
			TeamChoice teamChoice = descriptor.getPropertyGetter().getAnnotation(TeamChoice.class);
			Preconditions.checkNotNull(teamChoice);
			if (teamChoice.value().length() != 0) {
				choices.addAll((List<TeamFacade>)ReflectionUtils
						.invokeStaticMethod(descriptor.getBeanClass(), teamChoice.value()));
			} else {
				choices.addAll(OneDev.getInstance(CacheManager.class).getTeams().values());
			}
			for (Iterator<TeamFacade> it = choices.iterator(); it.hasNext();) {
				if (!it.next().getProjectId().equals(OneContext.get().getProject().getId()))
					it.remove();
			}
		
	    	teams = new ArrayList<>();
			if (getModelObject() != null) {
				TeamManager teamManager = OneDev.getInstance(TeamManager.class);
				for (String teamName: getModelObject()) {
					Team team = teamManager.find(OneContext.get().getProject(), teamName);
					if (team != null && choices.contains(team.getFacade()))
						teams.add(team.getFacade());
				}
			} 
		} finally {
			OneContext.pop();
		}
		
		input = new TeamMultiChoice("input", new Model((Serializable)teams), new TeamChoiceProvider(choices)) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor, this);
			}
			
		};
        input.setConvertEmptyInputStringToNull(true);
        input.setRequired(descriptor.isPropertyRequired());
        input.setLabel(Model.of(getDescriptor().getDisplayName(this)));
        
		input.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> teamNames = new ArrayList<>();
		Collection<TeamFacade> teams = input.getConvertedInput();
		if (teams != null) {
			for (TeamFacade team: teams)
				teamNames.add(team.getName());
		} 
		return teamNames;
	}

}
