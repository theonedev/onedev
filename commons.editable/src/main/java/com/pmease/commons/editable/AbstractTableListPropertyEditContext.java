package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.BeanUtils;

@SuppressWarnings("serial")
public abstract class AbstractTableListPropertyEditContext<T> extends PropertyEditContext<T> {

	private Class<?> elementClass;
	
	private List<List<PropertyEditContext<T>>> elementContexts;
	
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
			elementContexts = new ArrayList<List<PropertyEditContext<T>>>();
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

	public List<List<PropertyEditContext<T>>> getElementContexts() {
		return elementContexts;
	}
	
	@SuppressWarnings("unchecked")
	private List<PropertyEditContext<T>> createElementPropertyContexts(Serializable element) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		List<PropertyEditContext<T>> propertyContexts = new ArrayList<PropertyEditContext<T>>();
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
	public Map<Serializable, EditContext<T>> getChildContexts() {
		Map<Serializable, EditContext<T>> childContexts = new LinkedHashMap<Serializable, EditContext<T>>();
		if (elementContexts != null) {
			for (int i=0; i<elementContexts.size(); i++) {
				final String errorMessagePrefix = "row " + (i+1) + ": ";
				final List<PropertyEditContext<T>> elementPropertyContexts = elementContexts.get(i);
				
				EditContext<T> elementContext = new AbstractEditContext<T>(getPropertyValue().get(i)) {

					@Override
					public Map<Serializable, EditContext<T>> getChildContexts() {
						Map<Serializable, EditContext<T>> childContexts = new LinkedHashMap<Serializable, EditContext<T>>();
						for (PropertyEditContext<T> each: elementPropertyContexts)
							childContexts.put(each.getPropertyName(), each);
						return childContexts;
					}

					@Override
					public void renderForEdit(T renderContext) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void renderForView(T renderContext) {
						throw new UnsupportedOperationException();
					}

					@Override
					public List<ValidationError> getValidationErrors(boolean recursive) {
						List<ValidationError> validationErrors = new ArrayList<ValidationError>();
						for (String each: AbstractTableListPropertyEditContext.this.validationErrorMessages) {
							if (each.startsWith(errorMessagePrefix))
								validationErrors.add(new ValidationError(each.substring(errorMessagePrefix.length())));
						}
						
						if (recursive) {
							for (Map.Entry<Serializable, EditContext<T>> eachEntry: getChildContexts().entrySet()) {
								for (ValidationError eachError: eachEntry.getValue().getValidationErrors(true)) {
									PropertyPath newPath = eachError.getPropertyPath().prepend(eachEntry.getKey());
									validationErrors.add(new ValidationError(newPath, eachError.getErrorMessage()));
								}
							}
						}
						return Collections.unmodifiableList(validationErrors);
					}

					@Override
					public void error(String errorMessage) {
						AbstractTableListPropertyEditContext.this.error(errorMessagePrefix + errorMessage);
					}

					@SuppressWarnings("unchecked")
					@Override
					public void doValidation() {
						for (Iterator<String> it = AbstractTableListPropertyEditContext.this.validationErrorMessages.iterator(); it.hasNext();) {
							if (it.next().startsWith(errorMessagePrefix))
								it.remove();
						}
						
						if (getBean() instanceof Validatable) {
							((Validatable<T>)getBean()).validate(this);
						}
					}

				};
				
				childContexts.put(i, elementContext);
			}
		}
		return childContexts;
	}
	
}
