package com.pmease.commons.wicket.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.ImplementationRegistry;

@SuppressWarnings("serial")
public abstract class AbstractPolymorphicListPropertyEditContext extends PropertyEditContext {

	private final List<Class<?>> implementations = new ArrayList<Class<?>>();

	private List<BeanEditContext> elementContexts;

	public AbstractPolymorphicListPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);

		Class<?> elementBaseClass = EditableUtils.getElementClass(getPropertyGetter().getGenericReturnType());
		Preconditions.checkNotNull(elementBaseClass);
		
		ImplementationRegistry registry = AppLoader.getInstance(ImplementationRegistry.class);
		implementations.addAll(registry.getImplementations(elementBaseClass));

		Preconditions.checkArgument(
				!implementations.isEmpty(), 
				"Can not find implementations for '" + elementBaseClass + "'.");
		
		EditableUtils.sortAnnotatedElements(implementations);
		
		propertyValueChanged(getPropertyValue());
	}

	protected void propertyValueChanged(Serializable propertyValue) {
		if (propertyValue != null) {
			EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
			elementContexts = new ArrayList<BeanEditContext>();
			for (Serializable element: getPropertyValue()) {
				elementContexts.add(registry.getBeanEditContext(element));
			}
		} else {
			elementContexts = null;
		}
	}
	
	public List<Class<?>> getImplementations() {
		return implementations;
	}

	@Override
	public void setPropertyValue(Serializable propertyValue) {
		super.setPropertyValue(propertyValue);
		propertyValueChanged(propertyValue);
	}

	public List<BeanEditContext> getElementContexts() {
		return elementContexts;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Serializable> getPropertyValue() {
		return (ArrayList<Serializable>) super.getPropertyValue();
	}

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
	public Map<Serializable, EditContext> getChildContexts() {
		Map<Serializable, EditContext> childContexts = new LinkedHashMap<Serializable, EditContext>();
		if (elementContexts != null) {
			for (int i=0; i<elementContexts.size(); i++)
				childContexts.put(i, elementContexts.get(i));
		}
		return childContexts;
	}
	
}
