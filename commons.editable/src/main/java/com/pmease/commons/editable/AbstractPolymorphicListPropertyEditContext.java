package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.ImplementationRegistry;

@SuppressWarnings("serial")
public abstract class AbstractPolymorphicListPropertyEditContext<T> extends PropertyEditContext<T> {

	private final List<Class<?>> implementations = new ArrayList<Class<?>>();

	private List<BeanEditContext<T>> elementContexts;

	public AbstractPolymorphicListPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);

		Class<?> elementBaseClass = getPropertyGetter().getReturnType();
		ImplementationRegistry registry = AppLoader.getInstance(ImplementationRegistry.class);
		implementations.addAll(registry.getImplementations(elementBaseClass));

		Preconditions.checkArgument(
				!implementations.isEmpty(), 
				"Can not find implementations for '" + elementBaseClass + "'.");
		
		EditableUtils.sortAnnotatedElements(implementations);
		
		propertyValueChanged(getPropertyValue());
	}

	@SuppressWarnings("unchecked")
	protected void propertyValueChanged(Serializable propertyValue) {
		if (propertyValue != null) {
			EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
			elementContexts = new ArrayList<BeanEditContext<T>>();
			for (Serializable element: getPropertyValue()) {
				elementContexts.add(registry.getBeanEditContext(element));
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

	public List<BeanEditContext<T>> getElementContexts() {
		return elementContexts;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Serializable> getPropertyValue() {
		return (ArrayList<Serializable>) super.getPropertyValue();
	}

	@SuppressWarnings("unchecked")
	public void addElement(int index, Serializable element) {
		List<Serializable> propertyValue = getPropertyValue();
		Preconditions.checkNotNull(propertyValue);
		propertyValue.add(index, element);
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		elementContexts.add(index, registry.getBeanEditContext(element));
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
			for (BeanEditContext<T> each: elementContexts)
				each.validate();
		}
	}

	@Override
	public List<ValidationError> findValidationErrors() {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		validationErrors.addAll(getValidationErrors());
		if (elementContexts != null) {
			for (int i=0; i<elementContexts.size(); i++) {
				BeanEditContext<T> elementContext = elementContexts.get(i);
				for (ValidationError eachError: elementContext.findValidationErrors()) {
					validationErrors.add(new ValidationError("[" + i + "]", eachError));
				}
			}
		}
		
		return validationErrors;
	}
		
}
