package io.onedev.server.web.editable.userchoice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ReflectionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cache.CacheManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.component.user.choice.UserChoiceProvider;
import io.onedev.server.web.component.user.choice.UserMultiChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.UserChoice;

@SuppressWarnings("serial")
public class UserMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private final List<UserFacade> choices = new ArrayList<>();
	
	private UserMultiChoice input;
	
	public UserMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		OneContext oneContext = new OneContext(this);
		
		OneContext.push(oneContext);
		try {
			UserChoice userChoice = descriptor.getPropertyGetter().getAnnotation(UserChoice.class);
			Preconditions.checkNotNull(userChoice);
			if (userChoice.value().length() != 0) {
				choices.addAll((List<UserFacade>)ReflectionUtils
						.invokeStaticMethod(descriptor.getBeanClass(), userChoice.value()));
			} else {
				choices.addAll(OneDev.getInstance(CacheManager.class).getUsers().values());
			}
		} finally {
			OneContext.pop();
		}

		List<UserFacade> users = new ArrayList<>();
		if (getModelObject() != null) {
			UserManager userManager = OneDev.getInstance(UserManager.class);
			for (String userName: getModelObject()) {
				User user = userManager.findByName(userName);
				if (user != null && choices.contains(user.getFacade()))
					users.add(user.getFacade());
			}
		} 
		
		input = new UserMultiChoice("input", new Model((Serializable)users), new UserChoiceProvider(choices)) {

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
	public ErrorContext getErrorContext(PathElement element) {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> userNames = new ArrayList<>();
		Collection<UserFacade> users = input.getConvertedInput();
		if (users != null) {
			for (UserFacade user: users)
				userNames.add(user.getName());
		} 
		return userNames;
	}

}
