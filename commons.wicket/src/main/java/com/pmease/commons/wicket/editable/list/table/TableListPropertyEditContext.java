package com.pmease.commons.wicket.editable.list.table;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.wicket.editable.AbstractEditContext;
import com.pmease.commons.wicket.editable.EditContext;
import com.pmease.commons.wicket.editable.EditSupportRegistry;
import com.pmease.commons.wicket.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class TableListPropertyEditContext extends PropertyEditContext {

	private Class<?> elementClass;
	
	private List<List<PropertyEditContext>> elementContexts;
	
	private transient List<Method> elementPropertyGetters;
	
	public TableListPropertyEditContext(Serializable bean, String propertyName) {
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
		Map<Serializable, EditContext> childContexts = new LinkedHashMap<>();
		if (elementContexts != null) {
			for (int i=0; i<elementContexts.size(); i++) {
				final String errorMessagePrefix = "Row " + (i+1) + ": ";
				
				final List<PropertyEditContext> elementPropertyContexts = elementContexts.get(i);
				
				EditContext elementContext = new AbstractEditContext(getPropertyValue().get(i)) {

					@Override
					public Map<Serializable, EditContext> getChildContexts() {
						Map<Serializable, EditContext> childContexts = new LinkedHashMap<>();
						for (PropertyEditContext each: elementPropertyContexts)
							childContexts.put(each.getPropertyName(), each);
						return childContexts;
					}

					@Override
					public Component renderForEdit(String componentId) {
						throw new UnsupportedOperationException();
					}

					@Override
					public Component renderForView(String componentId) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void addValidationError(String errorMessage) {
						TableListPropertyEditContext.this.addValidationError(errorMessagePrefix + errorMessage);
					}

				};
				
				childContexts.put(i, elementContext);
			}
		}
		return childContexts;
	}
	
	@Override
	public Component renderForEdit(String componentId) {
		return new TableListPropertyEditor(componentId, this);
	}

	@Override
	public Component renderForView(String componentId) {
		if (getElementContexts() != null) {
			return new TableListPropertyViewer(componentId, this);
		} else {
			return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
		}
	}

}
