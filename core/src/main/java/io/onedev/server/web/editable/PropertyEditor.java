package io.onedev.server.web.editable;

import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;

import io.onedev.launcher.loader.AppLoader;

@SuppressWarnings("serial")
public abstract class PropertyEditor<T> extends ValueEditor<T> {

	protected final PropertyDescriptor propertyDescriptor;
	
	public PropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<T> propertyModel) {
		super(id, propertyModel);
		
		this.propertyDescriptor = propertyDescriptor;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new INullAcceptingValidator<T>() {

			@Override
			public void validate(IValidatable<T> validatable) {
				Validator validator = AppLoader.getInstance(Validator.class);
				Set<?> violations = validator.validateValue(
						propertyDescriptor.getBeanClass(), 
						propertyDescriptor.getPropertyName(), 
						validatable.getValue());
				
				for (Object each: violations) {
					ConstraintViolation<?> violation = (ConstraintViolation<?>) each;
					addError(violation.getMessage());
				}
			}
			
		});
		
		add(new AttributeAppender("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String classes = " property editor editable ";
				if (hasErrors(true) && getErrorClass() != null)
					classes += getErrorClass();
				return classes;
			}
			
		}));
		
	}
	
	@Nullable
	protected String getErrorClass() {
		return "has-error";
	}
	
	protected void onPropertyUpdating(IPartialPageRequestHandler target) {
		validate();
		if (!hasErrors(true)) 
			send(getParent(), Broadcast.BUBBLE, new PropertyUpdating(target, propertyDescriptor.getPropertyName()));								
		else
			clearErrors(true);
	}
	
	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}

}
