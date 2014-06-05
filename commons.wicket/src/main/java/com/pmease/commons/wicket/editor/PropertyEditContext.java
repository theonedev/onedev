package com.pmease.commons.wicket.editor;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.editable.PropertyDescriptorImpl;
import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
public abstract class PropertyEditContext<T extends Serializable> extends PropertyDescriptorImpl {

	public PropertyEditContext(Class<? extends Serializable> beanClass, String propertyName) {
		super(beanClass, propertyName);
	}
	
	public PropertyEditContext(PropertyDescriptor propertyDescriptor) {
		super(propertyDescriptor);
	}

	public abstract Component renderForView(String componentId, IModel<T> model);

	public abstract PropertyEditor<T> renderForEdit(String componentId, IModel<T> model);

	public static PropertyEditor<Serializable> edit(String componentId, final IModel<Serializable> beanModel, String propertyName) {
		final PropertyEditContext<Serializable> editContext = of(beanModel.getObject().getClass(), propertyName);
		return editContext.renderForEdit(componentId, new IModel<Serializable>() {

			@Override
			public void detach() {
				beanModel.detach();
			}

			@Override
			public Serializable getObject() {
				return editContext.getPropertyValue(beanModel.getObject());
			}

			@Override
			public void setObject(Serializable object) {
				editContext.setPropertyValue(beanModel.getObject(), object);
			}
			
		});
	}

	public static PropertyEditor<Object> edit(String componentId, final Serializable bean, String propertyName) {
		IModel<Object> beanModel = new IModel<Object>() {

			@Override
			public void detach() {
			}

			@Override
			public Object getObject() {
				return bean;
			}

			@Override
			public void setObject(Object object) {
				throw new IllegalStateException();
			}
			
		};
		return edit(componentId, beanModel, propertyName);
	}

	public static Component view(String componentId, final IModel<Serializable> beanModel, String propertyName) {
		final PropertyEditContext<Serializable> editContext = of(beanModel.getObject().getClass(), propertyName);
		return editContext.renderForView(componentId, new LoadableDetachableModel<Serializable>() {

			@Override
			protected Serializable load() {
				return editContext.getPropertyValue(beanModel.getObject());
			}
			
		});
	}

	public static Component view(String componentId, Serializable bean, String propertyName) {
		return view(componentId, Model.of(bean), propertyName);
	}

	public static PropertyEditContext<Serializable> of(Class<? extends Serializable> beanClass, String propertyName) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		return registry.getPropertyEditContext(beanClass, propertyName);
	}
	
	public static PropertyEditContext<Serializable> of(PropertyDescriptor propertyDescriptor) {
		return of(propertyDescriptor.getBeanClass(), propertyDescriptor.getPropertyName());
	}
}
