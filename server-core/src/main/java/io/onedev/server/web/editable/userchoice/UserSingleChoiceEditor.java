package io.onedev.server.web.editable.userchoice;

import static io.onedev.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.UserChoice;
import io.onedev.server.service.UserService;
import io.onedev.server.model.User;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.component.user.choice.UserSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

public class UserSingleChoiceEditor extends PropertyEditor<String> {

	private UserSingleChoice input;
	
	public UserSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	private UserService getUserService() {
		return OneDev.getInstance(UserService.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		UserCache cache = getUserService().cloneCache();
		
		List<Long> choiceIds;
		
		ComponentContext componentContext = new ComponentContext(this);
		ComponentContext.push(componentContext);
		try {
			UserChoice userChoice = descriptor.getPropertyGetter().getAnnotation(UserChoice.class);
			Preconditions.checkNotNull(userChoice);
			if (userChoice.value().length() != 0) {
				List<User> users = (List<User>) ReflectionUtils
						.invokeStaticMethod(descriptor.getBeanClass(), userChoice.value());
				choiceIds = users.stream().map(it->it.getId()).collect(toList());
			} else {
				choiceIds = cache.entrySet().stream()
						.filter(it -> !it.getValue().isDisabled())
						.map(it->it.getKey())
						.collect(toList());
				choiceIds.sort(Comparator.comparing(it -> cache.get(it).getDisplayName()));
			}
		} finally {
			ComponentContext.pop();
		}
		
		AtomicReference<Long> selectionId = new AtomicReference<>(null);
		
		if (getModelObject() != null) {
			UserFacade user = cache.findByName(getModelObject());
			if (user != null && choiceIds.contains(user.getId()))
				selectionId.set(user.getId());
		}
		
    	input = new UserSingleChoice("input", new IModel<User>() {

			@Override
			public void detach() {
			}

			@Override
			public User getObject() {
				return selectionId.get()!=null? getUserService().load(selectionId.get()): null;
			}

			@Override
			public void setObject(User object) {
				selectionId.set(User.idOf(object));
			}
    		
    	}, new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				return choiceIds.stream().map(it -> getUserService().load(it)).collect(toList());
			}
    		
    	}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
				getSettings().setAllowClear(!descriptor.isPropertyRequired());
			}
    		
    	};
        
        input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
        
		input.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		add(input);
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		User user = input.getConvertedInput();
		if (user != null)
			return user.getName();
		else
			return null;
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
