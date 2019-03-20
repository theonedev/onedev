package io.onedev.server.web.editable.bean;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class BeanPropertyEditor extends PropertyEditor<Serializable> {

	private final String BEAN_EDITOR_ID = "beanEditor";
	
	public BeanPropertyEditor(String id, PropertyDescriptor descriptor, IModel<Serializable> propertyModel) {
		super(id, descriptor, propertyModel);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (getDescriptor().isPropertyRequired()) {
			add(new WebMarkupContainer("enable").setVisible(false));
		} else {
			add(new CheckBox("enable", new IModel<Boolean>() {
				
				@Override
				public void detach() {
					
				}
	
				@Override
				public Boolean getObject() {
					Component beanEditor = BeanPropertyEditor.this.get(BEAN_EDITOR_ID);
					return beanEditor != null && beanEditor.isVisible();
				}
	
				@Override
				public void setObject(Boolean object) {
					Component beanEditor = BeanPropertyEditor.this.get(BEAN_EDITOR_ID);
					if (beanEditor instanceof BeanEditor || !object) {
						beanEditor.setVisible(object);
					} else {
						BeanPropertyEditor.this.replace(newBeanEditor(newProperty()));
					}
				}
				
			}).add(new AjaxFormComponentUpdatingBehavior("click"){

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					onPropertyUpdating(target);
					target.add(BeanPropertyEditor.this.get(BEAN_EDITOR_ID));
				}
				
			}));
			
		}

		Serializable propertyValue = getModelObject();
		
		if (getDescriptor().isPropertyRequired() && propertyValue == null) {
			propertyValue = newProperty();
		}
		add(newBeanEditor(propertyValue));
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof BeanUpdating) {
			event.stop();
			onPropertyUpdating(((BeanUpdating)event.getPayload()).getHandler());
		}		
	}
	
	private Serializable newProperty() {
		try {
			return (Serializable) getDescriptor().getPropertyClass().newInstance();
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
	public ErrorContext getErrorContext(PathElement element) {
		return ((ErrorContext) get(BEAN_EDITOR_ID)).getErrorContext(element);
	}

	@Override
	protected String getErrorClass() {
		return null;
	}

	@Override
	protected Serializable convertInputToValue() throws ConversionException {
		Component beanEditor = get(BEAN_EDITOR_ID);
		if (beanEditor.isVisible())
			return ((BeanEditor) beanEditor).getConvertedInput();
		else
			return null;
	}

}
