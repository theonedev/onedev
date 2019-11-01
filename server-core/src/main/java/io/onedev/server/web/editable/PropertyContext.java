package io.onedev.server.web.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.hibernate.proxy.HibernateProxyHelper;

import com.google.common.collect.Lists;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.util.ComponentContext;

@SuppressWarnings("serial")
public abstract class PropertyContext<T> implements Serializable {

	private final PropertyDescriptor descriptor;
	
	public PropertyContext(Class<?> beanClass, String propertyName) {
		descriptor = new PropertyDescriptor(beanClass, propertyName);
	}
	
	public PropertyContext(PropertyDescriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	public PropertyDescriptor getDescriptor() {
		return descriptor;
	}
	
	public abstract PropertyViewer renderForView(String componentId, IModel<T> model);

	public abstract PropertyEditor<T> renderForEdit(String componentId, IModel<T> model);
	
	public List<String> getPossibleValues() {
		return Lists.newArrayList();
	}

	public static PropertyEditor<Serializable> editModel(String componentId, IModel<Serializable> beanModel, String propertyName) {
		
		PropertyContext<Serializable> editContext = of(HibernateProxyHelper.getClassWithoutInitializingProxy(beanModel.getObject()), propertyName);
		return editContext.renderForEdit(componentId, new IModel<Serializable>() {

			@Override
			public void detach() {
				beanModel.detach();
			}

			@Override
			public Serializable getObject() {
				return (Serializable) editContext.getDescriptor().getPropertyValue(beanModel.getObject());
			}

			@Override
			public void setObject(Serializable object) {
				editContext.getDescriptor().setPropertyValue(beanModel.getObject(), object);
			}
			
		});
	}

	public static PropertyEditor<Serializable> edit(String componentId, Serializable bean, String propertyName) {
		
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

	public static Component viewModel(String componentId, IModel<Serializable> beanModel, String propertyName) {
		PropertyContext<Serializable> editContext = of(HibernateProxyHelper.getClassWithoutInitializingProxy(beanModel.getObject()), propertyName);
		return editContext.renderForView(componentId, new LoadableDetachableModel<Serializable>() {

			@Override
			protected Serializable load() {
				return (Serializable) editContext.getDescriptor().getPropertyValue(beanModel.getObject());
			}
			
		});
	}

	public static Component view(String componentId, Serializable bean, String propertyName) {
		return viewModel(componentId, Model.of(bean), propertyName);
	}

	public static PropertyContext<Serializable> of(Class<?> beanClass, String propertyName) {
		return of(new PropertyDescriptor(beanClass, propertyName));
	}
	
	public static PropertyContext<Serializable> of(PropertyDescriptor propertyDescriptor) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		return registry.getPropertyEditContext(propertyDescriptor);
	}
	
	public String getDisplayName() {
		return descriptor.getDisplayName();
	}
	
	public String getPropertyName() {
		return descriptor.getPropertyName();
	}
	
	public Method getPropertyGetter() {
		return descriptor.getPropertyGetter();
	}
	
	public Class<?> getPropertyClass() {
		return descriptor.getPropertyClass();
	}
	
	public Object getPropertyValue(Object bean) {
		return descriptor.getPropertyValue(bean);
	}
	
	public void setPropertyValue(Object bean, Object value) {
		descriptor.setPropertyValue(bean, value);
	}
	
	public boolean isPropertyExcluded()	{
		return descriptor.isPropertyExcluded();
	}
	
	public void setPropertyExcluded(boolean propertyExcluded) {
		descriptor.setPropertyExcluded(propertyExcluded);
	}
	
	public boolean isPropertyRequired() {
		return descriptor.isPropertyRequired();
	}

	public boolean isPropertyVisible(Map<String, ComponentContext> componentContexts, BeanDescriptor beanDescriptor) {
		return descriptor.isPropertyVisible(componentContexts, beanDescriptor);
	}
	
}
