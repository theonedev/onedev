package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.BeanUtils;

@SuppressWarnings("serial")
public abstract class AbstractTableListPropertyEditContext extends PropertyEditContext {

	private Class<?> elementClass;
	
	private List<List<PropertyEditContext>> elementContexts;
	
	private transient List<Method> elementPropertyGetters;
	
	public AbstractTableListPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);

		elementClass = EditableUtils.getElementClass(getPropertyGetter().getGenericReturnType());			
		Preconditions.checkNotNull(elementClass);
		
		propertyValueChanged(getPropertyValue());
	}
	
	public List<Method> getElementPropertyGetters() {
		if (elementPropertyGetters == null) {
			elementPropertyGetters = new ArrayList<Method>();
			
			for (Method each: BeanUtils.findGetters(elementClass)) {
				if (each.getAnnotation(Editable.class) != null && BeanUtils.getSetter(each) != null) {
					elementPropertyGetters.add(each);
				}
			}
			EditableUtils.sortAnnotatedElements(elementPropertyGetters);
		}
		return elementPropertyGetters;
	}
	
	public Class<?> getElementClass() {
		return elementClass;
	}
	
	protected void propertyValueChanged(Serializable propertyValue) {
		if (propertyValue != null) {
			elementContexts = new ArrayList<List<PropertyEditContext>>();
			for (Serializable element: getPropertyValue()) {
				elementContexts.add(createElementPropertyContexts(element));
			}
		} else {
			elementContexts = null;
		}
	}

	@Override
	public void setPropertyValue(Serializable propertyValue) {
		super.setPropertyValue(propertyValue);
		propertyValueChanged(propertyValue);
	}

	public List<List<PropertyEditContext>> getElementContexts() {
		return elementContexts;
	}
	
	private List<PropertyEditContext> createElementPropertyContexts(Serializable element) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		List<PropertyEditContext> propertyContexts = new ArrayList<PropertyEditContext>();
		for (Method elementPropertyGetter: getElementPropertyGetters()) {
			String elementPropertyName = BeanUtils.getPropertyName(elementPropertyGetter);
			propertyContexts.add(registry.getPropertyEditContext(element, elementPropertyName));
		}
		return propertyContexts;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Serializable> getPropertyValue() {
		return (ArrayList<Serializable>) super.getPropertyValue();
	}

	public void addElement(int index, Serializable elementValue) {
		List<Serializable> propertyValue = getPropertyValue();
		Preconditions.checkNotNull(propertyValue);
		propertyValue.add(index, elementValue);
		elementContexts.add(index, createElementPropertyContexts(elementValue));
	}
	
	public void removeElement(int index) {
		List<Serializable> propertyValue = getPropertyValue();
		Preconditions.checkNotNull(propertyValue);

		propertyValue.remove(index);
		elementContexts.remove(index);
	}
	
	@Override
	protected void doValidation() {
		super.doValidation();
		
		if (elementContexts != null) {
			for (int i=0; i<elementContexts.size(); i++) {
				List<PropertyEditContext> elementPropertyContexts = elementContexts.get(i);
				Set<String> propertyNames = new HashSet<String>();
				for (PropertyEditContext elementPropertyContext: elementPropertyContexts) {
					elementPropertyContext.validate();
					if (elementPropertyContext.findValidationErrors().isEmpty())
						propertyNames.add(elementPropertyContext.getPropertyName());
				}
				
				Serializable elementValue = getPropertyValue().get(i);
				if (elementValue instanceof Validatable) {
					final int index = i;
					((Validatable)elementValue).validate(propertyNames, new ErrorContext() {

						@Override
						public void error(String propertyPath, String errorMessage) {
							propertyPath = "[" + index + "]." + propertyPath;
							AbstractTableListPropertyEditContext.this.error(propertyPath, errorMessage);
						}

						@Override
						public void error(String errorMessage) {
							String propertyPath = "[" + index + "]";
							AbstractTableListPropertyEditContext.this.error(propertyPath, errorMessage);
						}
						
					});
				}
			}
		}
	}

	@Override
	public List<ValidationError> findValidationErrors() {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		validationErrors.addAll(getValidationErrors());
		
		if (elementContexts != null) {
			for (int i=0; i<elementContexts.size(); i++) {
				List<PropertyEditContext> elementPropertyContexts = elementContexts.get(i);
				for (PropertyEditContext elementPropertyContext: elementPropertyContexts) {
					for (ValidationError eachError: elementPropertyContext.findValidationErrors()) {
						String propertyPath = "[" + i + "]." + elementPropertyContext.getPropertyName();
						validationErrors.add(new ValidationError(propertyPath, eachError));
					}
				}
			}
		}
		
		return validationErrors;
	}
		
}
