package io.onedev.server.web.editable;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.launcher.loader.AppLoader;
import jersey.repackaged.com.google.common.collect.Lists;

@SuppressWarnings("serial")
public abstract class PropertyContext<T> extends PropertyDescriptor {

	public PropertyContext(Class<?> beanClass, String propertyName) {
		super(beanClass, propertyName);
	}
	
	public PropertyContext(PropertyDescriptor propertyDescriptor) {
		super(propertyDescriptor);
	}
	
	public abstract PropertyViewer renderForView(String componentId, IModel<T> model);

	public abstract PropertyEditor<T> renderForEdit(String componentId, IModel<T> model);
	
	public List<String> getPossibleValues() {
		return Lists.newArrayList();
	}

	public static PropertyEditor<Serializable> editModel(String componentId, 
			final IModel<Serializable> beanModel, String propertyName) {
		
		final PropertyContext<Serializable> editContext = of(beanModel.getObject().getClass(), propertyName);
		return editContext.renderForEdit(componentId, new IModel<Serializable>() {

			@Override
			public void detach() {
				beanModel.detach();
			}

			@Override
			public Serializable getObject() {
				return (Serializable) editContext.getPropertyValue(beanModel.getObject());
			}

			@Override
			public void setObject(Serializable object) {
				editContext.setPropertyValue(beanModel.getObject(), object);
			}
			
		});
	}

	public static PropertyEditor<Serializable> editBean(String componentId, 
			final Serializable bean, String propertyName) {
		
		IModel<Serializable> beanModel = new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return bean;
			}

			@Override
			public void setObject(Serializable object) {
				throw new IllegalStateException();
			}
			
		};
		return editModel(componentId, beanModel, propertyName);
	}

	public static Component viewModel(String componentId, final IModel<Serializable> beanModel, String propertyName) {
		final PropertyContext<Serializable> editContext = of(beanModel.getObject().getClass(), propertyName);
		return editContext.renderForView(componentId, new LoadableDetachableModel<Serializable>() {

			@Override
			protected Serializable load() {
				return (Serializable) editContext.getPropertyValue(beanModel.getObject());
			}
			
		});
	}

	public static Component viewBean(String componentId, Serializable bean, String propertyName) {
		return viewModel(componentId, Model.of(bean), propertyName);
	}

	public static PropertyContext<Serializable> of(Class<?> beanClass, String propertyName) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		return registry.getPropertyEditContext(beanClass, propertyName);
	}
	
	public static PropertyContext<Serializable> of(PropertyDescriptor propertyDescriptor) {
		PropertyContext<Serializable> propertyContext = of(propertyDescriptor.getBeanClass(), propertyDescriptor.getPropertyName());
		propertyContext.setExcluded(propertyDescriptor.isExcluded());
		propertyContext.setOptional(propertyDescriptor.isOptional());
		return propertyContext;
	}
	
}
