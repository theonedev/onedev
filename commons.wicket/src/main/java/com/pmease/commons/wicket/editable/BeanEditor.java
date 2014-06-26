package com.pmease.commons.wicket.editable;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;

import com.pmease.commons.editable.BeanDescriptor;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.editable.PathSegment.Property;

@SuppressWarnings("serial")
public abstract class BeanEditor<T> extends ValueEditor<T> {

	private final BeanDescriptor beanDescriptor;
	
	private final Set<String> propertyNames = new HashSet<>();
	
	public BeanEditor(String id, BeanDescriptor beanDescriptor, IModel<T> model) {
		super(id, model);
		
		this.beanDescriptor = beanDescriptor;
		
		for (PropertyDescriptor propertyDescriptor: beanDescriptor.getPropertyDescriptors())
			propertyNames.add(propertyDescriptor.getPropertyName());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (findParent(BeanEditor.class) == null) {
			add(new IValidator<T>() {
	
				@Override
				public void validate(IValidatable<T> validatable) {
					Validator validator = AppLoader.getInstance(Validator.class);
					for (ConstraintViolation<T> violation: validator.validate(validatable.getValue())) {
						ValuePath valuePath = new ValuePath(violation.getPropertyPath());
						if (!valuePath.getElements().isEmpty()) {
							PathSegment.Property property = (Property) valuePath.getElements().iterator().next();
							if (!propertyNames.contains(property.getName()))
								continue;
						}
						ErrorContext errorContext = getErrorContext(valuePath);
						errorContext.addError(violation.getMessage());
					}
				}
				
			});
		}
		
		add(AttributeAppender.append("class", " bean editor editable"));
	}
	
	public BeanDescriptor getBeanDescriptor() {
		return beanDescriptor;
	}

}
