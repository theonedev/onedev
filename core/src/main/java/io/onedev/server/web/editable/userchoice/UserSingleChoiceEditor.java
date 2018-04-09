package io.onedev.server.web.editable.userchoice;

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
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.annotation.UserChoice;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.component.userchoice.UserChoiceProvider;
import io.onedev.server.web.component.userchoice.UserSingleChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.utils.ReflectionUtils;

@SuppressWarnings("serial")
public class UserSingleChoiceEditor extends PropertyEditor<String> {

	private final List<UserFacade> choices = new ArrayList<>();
	
	private UserSingleChoice input;
	
	public UserSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		OneContext oneContext = new OneContext(this);
		
		OneContext.push(oneContext);
		try {
			UserChoice userChoice = propertyDescriptor.getPropertyGetter().getAnnotation(UserChoice.class);
			Preconditions.checkNotNull(userChoice);
			if (userChoice.value().length() != 0) {
				choices.addAll((List<UserFacade>)ReflectionUtils
						.invokeStaticMethod(propertyDescriptor.getBeanClass(), userChoice.value()));
			} else {
				choices.addAll(OneDev.getInstance(CacheManager.class).getUsers().values());
			}
		} finally {
			OneContext.pop();
		}
		
		User user;
		if (getModelObject() != null)
			user = OneDev.getInstance(UserManager.class).findByName(getModelObject());
		else
			user = null;
		
		UserFacade facade = user!=null?user.getFacade():null;
    	input = new UserSingleChoice("input", Model.of(facade), new UserChoiceProvider(choices));
        input.setConvertEmptyInputStringToNull(true);
        
        // add this to control allowClear flag of select2
    	input.setRequired(propertyDescriptor.isPropertyRequired());
        input.setLabel(Model.of(getPropertyDescriptor().getDisplayName(this)));
        
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
		UserFacade user = input.getConvertedInput();
		if (user != null)
			return user.getName();
		else
			return null;
	}

}
