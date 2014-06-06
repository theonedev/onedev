package com.pmease.commons.wicket.editor.reflection;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editor.BeanEditContext;
import com.pmease.commons.wicket.editor.BeanEditor;
import com.pmease.commons.wicket.editor.ErrorContext;
import com.pmease.commons.wicket.editor.PathSegment;
import com.pmease.commons.wicket.editor.PropertyEditor;

@SuppressWarnings("serial")
public class ReflectionPropertyEditor extends PropertyEditor<Serializable> {

	private final String BEAN_EDITOR_ID = "beanEditor";
	
	private Serializable propertyValue;

	public ReflectionPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Serializable> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		
		propertyValue = propertyModel.getObject();
		
		if (propertyDescriptor.isPropertyRequired() && propertyValue == null) {
			try {
				propertyValue = (Serializable) propertyDescriptor.getPropertyClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
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
					return propertyValue != null;
				}
	
				@Override
				public void setObject(Boolean object) {
					if (object) {
						try {
							propertyValue = (Serializable) getPropertyDescriptor().getPropertyClass().newInstance();
						} catch (InstantiationException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					} else {
						propertyValue = null;
					}
				}
				
			}).add(new AjaxFormComponentUpdatingBehavior("onclick"){

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					Component beanEditor = newBeanEditor();
					replace(beanEditor);
					target.add(beanEditor);
				}
				
			}));
			
		}

		add(newBeanEditor());
	}
	
	private Component newBeanEditor() {
		Component beanEditor;
		if (propertyValue != null) {
			beanEditor = BeanEditContext.edit(BEAN_EDITOR_ID, propertyValue);
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
		if (propertyValue != null)
			return ((BeanEditor<Serializable>) get(BEAN_EDITOR_ID)).getConvertedInput();
		else
			return null;
	}

}
