package io.onedev.server.web.editable.userchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.component.user.choice.UserSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.annotation.UserChoice;

@SuppressWarnings("serial")
public class UserSingleChoiceEditor extends PropertyEditor<String> {

	private UserSingleChoice input;
	
	public UserSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		UserCache cache = getUserManager().cloneCache();
		
		List<Long> choiceIds;
		
		ComponentContext componentContext = new ComponentContext(this);
		ComponentContext.push(componentContext);
		try {
			UserChoice userChoice = descriptor.getPropertyGetter().getAnnotation(UserChoice.class);
			Preconditions.checkNotNull(userChoice);
			if (userChoice.value().length() != 0) {
				List<User> users = (List<User>) ReflectionUtils
						.invokeStaticMethod(descriptor.getBeanClass(), userChoice.value());
				choiceIds = users.stream().map(it->it.getId()).collect(Collectors.toList());
			} else {
				choiceIds = new ArrayList<>(cache.keySet());
				choiceIds.removeIf(it->it<0);
				choiceIds.sort(new Comparator<Long>() {

					@Override
					public int compare(Long o1, Long o2) {
						return cache.get(o1).getDisplayName().compareTo(cache.get(o2).getDisplayName());
					}
					
				});
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
				return selectionId.get()!=null? getUserManager().load(selectionId.get()): null;
			}

			@Override
			public void setObject(User object) {
				selectionId.set(User.idOf(object));
			}
    		
    	}, new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				return choiceIds.stream().map(it -> getUserManager().load(it)).collect(Collectors.toList());
			}
    		
    	}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
				getSettings().setAllowClear(!descriptor.isPropertyRequired());
			}
    		
    	};
        
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
