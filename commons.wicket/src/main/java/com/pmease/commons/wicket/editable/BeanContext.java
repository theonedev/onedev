package com.pmease.commons.wicket.editable;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.editable.BeanDescriptor;
import com.pmease.commons.editable.BeanDescriptorImpl;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.ClassUtils;

@SuppressWarnings("serial")
public abstract class BeanContext<T> extends BeanDescriptorImpl {
	
	public BeanContext(Class<?> beanClass) {
		super(beanClass);
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

	@SuppressWarnings("unchecked")
	public static BeanEditor<Serializable> editModel(String componentId, IModel<? extends Serializable> beanModel) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		Class<?> beanClass = ClassUtils.unproxy(beanModel.getObject().getClass());
		BeanContext<Serializable> editContext = registry.getBeanEditContext(beanClass);
		return editContext.renderForEdit(componentId, (IModel<Serializable>)beanModel);
	}
	
	public static BeanEditor<Serializable> editBean(String componentId, final Serializable bean) {
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
		BeanContext<Serializable> editContext = registry.getBeanEditContext(beanClass);
		beanModel = editContext.wrapAsSelfUpdating(beanModel);
		return editContext.renderForEdit(componentId, beanModel);
	}

	public static Component viewModel(String componentId, IModel<Serializable> beanModel) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		BeanContext<Serializable> editContext = registry.getBeanEditContext(beanModel.getObject().getClass());
		return editContext.renderForView(componentId, beanModel);
	}
	
	public static Component viewBean(String componentId, Serializable bean) {
		return viewModel(componentId, Model.of(bean));
	}
	
	public static BeanContext<Serializable> of(Class<?> beanClass) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		return registry.getBeanEditContext(beanClass);
	}
	
	public static BeanContext<Serializable> of(BeanDescriptor beanDescriptor) {
		return of(beanDescriptor.getBeanClass());
	}
}
