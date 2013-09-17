package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.BeanUtils;

@SuppressWarnings("serial")
public abstract class AbstractReflectionBeanEditContext<T> extends BeanEditContext<T> {

	private List<PropertyEditContext<T>> propertyContexts = new ArrayList<PropertyEditContext<T>>();
	
	@SuppressWarnings("unchecked")
	public AbstractReflectionBeanEditContext(Serializable bean) {
		super(bean);

		List<Method> propertyGetters = BeanUtils.findGetters(getBean().getClass());
		EditableUtils.sortAnnotatedElements(propertyGetters);
		
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		for (Method propertyGetter: propertyGetters) {
			if (propertyGetter.getAnnotation(Editable.class) != null && BeanUtils.getSetter(propertyGetter) != null) {
				propertyContexts.add(registry.getPropertyEditContext(bean, BeanUtils.getPropertyName(propertyGetter)));
			}
		}
	}

	public List<PropertyEditContext<T>> getPropertyContexts() {
		return propertyContexts;
	}

	@Override
	public void doValidation() {
		Set<String> propertyNames = new HashSet<String>();
		for (PropertyEditContext<T> each: propertyContexts) {
			each.validate();
			if (each.findValidationErrors().isEmpty())
				propertyNames.add(each.getPropertyName());
		}
		
		if (getBean() instanceof Validatable) {
			((Validatable)getBean()).validate(propertyNames, new ErrorContext() {

				@Override
				public void error(String propertyPath, String errorMessage) {
					AbstractReflectionBeanEditContext.this.error(propertyPath, errorMessage);
				}

				@Override
				public void error(String errorMessage) {
					AbstractReflectionBeanEditContext.this.error(errorMessage);
				}
				
			});
		}
	}

	@Override
	public List<ValidationError> findValidationErrors() {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		validationErrors.addAll(getValidationErrors());

		for (PropertyEditContext<T> eachContext: propertyContexts) {
			for (ValidationError eachError: eachContext.findValidationErrors()) {
				validationErrors.add(new ValidationError(eachContext.getPropertyName(), eachError));
			}
		}
		return validationErrors;
	}

}
