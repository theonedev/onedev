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
	public Map<Serializable, EditContext> getChildContexts() {
		Map<Serializable, EditContext> childContexts = new LinkedHashMap<Serializable, EditContext>();
		if (elementContexts != null) {
			for (int i=0; i<elementContexts.size(); i++) {
				final String errorMessagePrefix = "row " + (i+1) + ": ";
				final List<PropertyEditContext> elementPropertyContexts = elementContexts.get(i);
				
				EditContext elementContext = new AbstractEditContext(getPropertyValue().get(i)) {

					@Override
					public Map<Serializable, EditContext> getChildContexts() {
						Map<Serializable, EditContext> childContexts = new LinkedHashMap<Serializable, EditContext>();
						for (PropertyEditContext each: elementPropertyContexts)
							childContexts.put(each.getPropertyName(), each);
						return childContexts;
					}

					@Override
					public Object renderForEdit(Object renderParam) {
						throw new UnsupportedOperationException();
					}

					@Override
					public Object renderForView(Object renderParam) {
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
							for (Map.Entry<Serializable, EditContext> eachEntry: getChildContexts().entrySet()) {
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

					@Override
					public void doValidation() {
						for (Iterator<String> it = AbstractTableListPropertyEditContext.this.validationErrorMessages.iterator(); it.hasNext();) {
							if (it.next().startsWith(errorMessagePrefix))
								it.remove();
						}
						
						if (getBean() instanceof Validatable) {
							((Validatable)getBean()).validate(this);
						}
					}

				};
				
				childContexts.put(i, elementContext);
			}
		}
		return childContexts;
	}
	
}
