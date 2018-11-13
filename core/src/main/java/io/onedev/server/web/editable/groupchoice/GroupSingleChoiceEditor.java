package io.onedev.server.web.editable.groupchoice;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.web.component.groupchoice.GroupChoiceProvider;
import io.onedev.server.web.component.groupchoice.GroupSingleChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.GroupChoice;
import io.onedev.utils.ReflectionUtils;

@SuppressWarnings("serial")
public class GroupSingleChoiceEditor extends PropertyEditor<String> {
	
	private final List<GroupFacade> choices = new ArrayList<>();
	
	private GroupSingleChoice input;
	
	public GroupSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		GroupFacade facade;

		GroupChoice groupChoice = descriptor.getPropertyGetter().getAnnotation(GroupChoice.class);
		Preconditions.checkNotNull(groupChoice);
		if (groupChoice.value().length() != 0) {
			choices.addAll((List<GroupFacade>)ReflectionUtils
					.invokeStaticMethod(descriptor.getBeanClass(), groupChoice.value()));
		} else {
			choices.addAll(OneDev.getInstance(CacheManager.class).getGroups().values());
		}
		
		Group group;
		if (getModelObject() != null)
			group = OneDev.getInstance(GroupManager.class).find(getModelObject());
		else
			group = null;
		
		if (group != null && choices.contains(group.getFacade()))
			facade = group.getFacade();
		else
			facade = null;

    	input = new GroupSingleChoice("input", Model.of(facade), new GroupChoiceProvider(choices)) {

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
		GroupFacade group = input.getConvertedInput();
		if (group != null)
			return group.getName();
		else
			return null;
	}

}
