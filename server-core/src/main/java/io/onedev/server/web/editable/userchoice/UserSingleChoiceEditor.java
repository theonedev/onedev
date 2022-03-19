package io.onedev.server.web.editable.userchoice;

import java.util.ArrayList;
import java.util.Collection;
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
import io.onedev.server.web.component.user.choice.UserSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.UserChoice;

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
				choices.addAll(getUserManager().query());
				choices.sort(Comparator.comparing(User::getDisplayName));
			}
		} finally {
			ComponentContext.pop();
		}
		
		User selection;
		if (getModelObject() != null)
			selection = getUserManager().findByName(getModelObject());
		else
			selection = null;
		
		if (selection != null && !choices.contains(selection))
			selection = null;
		
		List<Long> choiceIds = choices.stream().map(it->it.getId()).collect(Collectors.toList());
		
		AtomicReference<Long> selectionId = new AtomicReference<>(null);
		if (selection != null)
			selectionId.set(selection.getId());
		
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
    		
    	}, new LoadableDetachableModel<Collection<User>>() {

			@Override
			protected Collection<User> load() {
				return choiceIds.stream().map(it-> getUserManager().load(it)).collect(Collectors.toList());
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
