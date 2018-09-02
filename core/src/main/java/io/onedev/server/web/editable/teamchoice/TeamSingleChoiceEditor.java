package io.onedev.server.web.editable.teamchoice;

import java.util.ArrayList;
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
import io.onedev.server.web.component.teamchoice.TeamSingleChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.TeamChoice;
import io.onedev.server.web.util.ComponentContext;
import io.onedev.utils.ReflectionUtils;

@SuppressWarnings("serial")
public class TeamSingleChoiceEditor extends PropertyEditor<String> {
	
	private final List<TeamFacade> choices = new ArrayList<>();
	
	private TeamSingleChoice input;
	
	public TeamSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		TeamFacade facade;

		OneContext oneContext = new ComponentContext(this);
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
			
			Team team;
			if (getModelObject() != null)
				team = OneDev.getInstance(TeamManager.class).find(OneContext.get().getProject(), getModelObject());
			else
				team = null;
			
			if (team != null && choices.contains(team.getFacade()))
				facade = team.getFacade();
			else
				facade = null;
		} finally {
			OneContext.pop();
		}

    	input = new TeamSingleChoice("input", Model.of(facade), new TeamChoiceProvider(choices)) {

    		@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor, this);
			}
    		
    	};
        input.setConvertEmptyInputStringToNull(true);

        // add this to control allowClear flag of select2
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
	protected String convertInputToValue() throws ConversionException {
		TeamFacade team = input.getConvertedInput();
		if (team != null)
			return team.getName();
		else
			return null;
	}

}
