package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;

@SuppressWarnings("serial")
public class ReflectionPropertyEditor extends PropertyEditor<Serializable> {

	private final String BEAN_EDITOR_ID = "beanEditor";
	
	public ReflectionPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Serializable> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (getPropertyDescriptor().isPropertyRequired()) {
			add(new WebMarkupContainer("enable").setVisible(false));
		} else {
			add(new CheckBox("enable", new IModel<Boolean>() {
				
				@Override
				public void detach() {
					
				}
	
				@Override
				public Boolean getObject() {
					return ReflectionPropertyEditor.this.get(BEAN_EDITOR_ID).isVisible();
				}
	
				@Override
				public void setObject(Boolean object) {
					Component beanEditor = ReflectionPropertyEditor.this.get(BEAN_EDITOR_ID);
					if (beanEditor instanceof BeanEditor || !object) {
						beanEditor.setVisible(object);
					} else {
						ReflectionPropertyEditor.this.replace(newBeanEditor(newProperty()));
					}
				}
				
			}).add(new AjaxFormComponentUpdatingBehavior("onclick"){

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					target.add(ReflectionPropertyEditor.this.get(BEAN_EDITOR_ID));
				}
				
			}));
			
		}

		Serializable propertyValue = getModelObject();
		
		if (getPropertyDescriptor().isPropertyRequired() && propertyValue == null) {
			propertyValue = newProperty();
		}
		add(newBeanEditor(propertyValue));
	}
	
	private Serializable newProperty() {
		try {
			return (Serializable) getPropertyDescriptor().getPropertyClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Component newBeanEditor(Serializable propertyValue) {
		Component beanEditor;
		if (propertyValue != null) {
			beanEditor = BeanContext.editBean(BEAN_EDITOR_ID, propertyValue);
		} else {
			beanEditor = new WebMarkupContainer(BEAN_EDITOR_ID).setVisible(false);
		}
		beanEditor.setOutputMarkupId(true);
		beanEditor.setOutputMarkupPlaceholderTag(true);
		return beanEditor;
	}
		
	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return ((ErrorContext) get(BEAN_EDITOR_ID)).getErrorContext(pathSegment);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Serializable convertInputToValue() throws ConversionException {
		Component beanEditor = get(BEAN_EDITOR_ID);
		if (beanEditor.isVisible())
			return ((BeanEditor<Serializable>) beanEditor).getConvertedInput();
		else
			return null;
	}

}
