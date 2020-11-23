package io.onedev.server.web.editable.userchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.component.user.choice.UserMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.UserChoice;

@SuppressWarnings("serial")
public class UserMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private UserMultiChoice input;
	
	public UserMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({"unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<User> choices = new ArrayList<>();
		
		ComponentContext componentContext = new ComponentContext(this);
		ComponentContext.push(componentContext);
		try {
			UserChoice userChoice = descriptor.getPropertyGetter().getAnnotation(UserChoice.class);
			Preconditions.checkNotNull(userChoice);
			if (userChoice.value().length() != 0) {
				choices.addAll((List<User>)ReflectionUtils
						.invokeStaticMethod(descriptor.getBeanClass(), userChoice.value()));
			} else {
				choices.addAll(OneDev.getInstance(UserManager.class).query());
				choices.sort(Comparator.comparing(User::getDisplayName));
			}
		} finally {
			ComponentContext.pop();
		}

		List<User> selections = new ArrayList<>();
		if (getModelObject() != null) {
			UserManager userManager = OneDev.getInstance(UserManager.class);
			for (String userName: getModelObject()) {
				User user = userManager.findByName(userName);
				if (user != null && choices.contains(user))
					selections.add(user);
			}
		} 
		
		input = new UserMultiChoice("input", Model.of(selections), Model.of(choices)) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}

		};
        
        input.setRequired(descriptor.isPropertyRequired());
        input.setLabel(Model.of(getDescriptor().getDisplayName()));
        
		input.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		
        add(input);
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		Collection<User> users = input.getConvertedInput();
		if (users != null) 
			return users.stream().map(it->it.getName()).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

}
