package com.pmease.commons.wicket.editable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.ClassUtils;

@SuppressWarnings("serial")
public abstract class BeanContext<T> extends DefaultBeanDescriptor {
	
	public BeanContext(Class<?> beanClass, Set<String> excludeProperties) {
		super(beanClass, excludeProperties);
	}
	
	public BeanContext(Class<?> beanClass) {
		super(beanClass, new HashSet<>());
	}
	
	public BeanContext(BeanDescriptor beanDescriptor) {
		super(beanDescriptor);
	}
	
	public abstract BeanViewer renderForView(String componentId, IModel<T> model);

	public abstract BeanEditor<T> renderForEdit(String componentId, IModel<T> model);
	
	public IModel<T> wrapAsSelfUpdating(final IModel<T> model) {
		return new IModel<T>() {

			@Override
			public void detach() {
				model.detach();
			}

			@Override
			public T getObject() {
				return model.getObject();
			}

			@Override
			public void setObject(T object) {
				copyProperties(object, getObject());
			}
			
		};
	}

	public static BeanEditor<Serializable> editModel(String componentId, 
			IModel<? extends Serializable> beanModel) {
		return editModel(componentId, beanModel, new HashSet<>());
	}
			
	@SuppressWarnings("unchecked")
	public static BeanEditor<Serializable> editModel(String componentId, 
			IModel<? extends Serializable> beanModel, Set<String> excludeProperties) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		Class<?> beanClass = ClassUtils.unproxy(beanModel.getObject().getClass());
		BeanContext<Serializable> editContext = registry.getBeanEditContext(beanClass, excludeProperties);
		return editContext.renderForEdit(componentId, (IModel<Serializable>)beanModel);
	}
	
	public static BeanEditor<Serializable> editBean(String componentId, Serializable bean) {
		return editBean(componentId, bean, new HashSet<>());
	}
			
	public static BeanEditor<Serializable> editBean(String componentId, final Serializable bean, 
			Set<String> excludeProperties) {
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
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		Class<?> beanClass = ClassUtils.unproxy(beanModel.getObject().getClass());
		BeanContext<Serializable> editContext = registry.getBeanEditContext(beanClass, excludeProperties);
		beanModel = editContext.wrapAsSelfUpdating(beanModel);
		return editContext.renderForEdit(componentId, beanModel);
	}

	public static Component viewModel(String componentId, IModel<Serializable> beanModel) {
		return viewModel(componentId, beanModel, new HashSet<>());
	}
			
	public static Component viewModel(String componentId, IModel<Serializable> beanModel, 
			Set<String> excludeProperties) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		BeanContext<Serializable> editContext = registry.getBeanEditContext(
				beanModel.getObject().getClass(), excludeProperties);
		return editContext.renderForView(componentId, beanModel);
	}
	
	public static Component viewBean(String componentId, Serializable bean) {
		return viewBean(componentId, bean, new HashSet<>());
	}
	
	public static Component viewBean(String componentId, Serializable bean, Set<String> excludeProperties) {
		return viewModel(componentId, Model.of(bean), excludeProperties);
	}
	
	public static BeanContext<Serializable> of(Class<?> beanClass) {
		return of(beanClass, new HashSet<>());
	}
	
	public static BeanContext<Serializable> of(Class<?> beanClass, Set<String> excludeProperties) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		return registry.getBeanEditContext(beanClass, excludeProperties);
	}
	
}
