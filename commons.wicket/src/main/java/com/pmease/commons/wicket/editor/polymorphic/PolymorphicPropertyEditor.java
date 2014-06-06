package com.pmease.commons.wicket.editor.polymorphic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.ImplementationRegistry;
import com.pmease.commons.wicket.editor.BeanEditContext;
import com.pmease.commons.wicket.editor.BeanEditor;
import com.pmease.commons.wicket.editor.ErrorContext;
import com.pmease.commons.wicket.editor.PathSegment;
import com.pmease.commons.wicket.editor.PropertyEditor;

@SuppressWarnings("serial")
public class PolymorphicPropertyEditor extends PropertyEditor<Serializable> {

	private static final String BEAN_EDITOR_ID = "beanEditor";
	
	private final List<Class<? extends Serializable>> implementations = new ArrayList<>();
	
	private Serializable propertyValue;
	
	public PolymorphicPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Serializable> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		
		Class<? extends Serializable> baseClass = propertyDescriptor.getPropertyClass();
		ImplementationRegistry registry = AppLoader.getInstance(ImplementationRegistry.class);
		implementations.addAll(registry.getImplementations(baseClass));
		
		Preconditions.checkArgument(
				!implementations.isEmpty(), 
				"Can not find implementations for '" + baseClass + "'.");
		
		EditableUtils.sortAnnotatedElements(implementations);
		
		propertyValue = propertyModel.getObject();
		
		if (getPropertyDescriptor().isPropertyRequired() && propertyValue == null) {
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

		List<String> implementationNames = new ArrayList<String>();
		for (Class<?> each: implementations)
			implementationNames.add(EditableUtils.getName(each));
				
		DropDownChoice<String> typeSelector = new DropDownChoice<String>("typeSelector", new IModel<String>() {
			
			@Override
			public void detach() {
				
			}

			@Override
			public String getObject() {
				if (propertyValue != null)
					return EditableUtils.getName(propertyValue.getClass());
				else
					return null;
			}

			@Override
			public void setObject(String object) {
				boolean found = false;
				
				for (Class<?> each: implementations) {
					if (EditableUtils.getName(each).equals(object)) {
						try {
							propertyValue = (Serializable) each.newInstance();
						} catch (InstantiationException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}
						found = true;
					}
				}
				
				if (!found)
					propertyValue = null;
			}
			
		}, implementationNames);
		
		typeSelector.setNullValid(!getPropertyDescriptor().isPropertyRequired());
		typeSelector.add(new AjaxFormComponentUpdatingBehavior("onclick"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				Component beanEditor = newBeanEditor();
				replace(beanEditor);
				target.add(beanEditor);
			}
			
		});
		
		add(typeSelector);

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
