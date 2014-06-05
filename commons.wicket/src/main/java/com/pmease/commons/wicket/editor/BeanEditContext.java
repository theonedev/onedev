package com.pmease.commons.wicket.editor;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.editable.BeanDescriptor;
import com.pmease.commons.editable.BeanDescriptorImpl;
import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
public abstract class BeanEditContext<T extends Serializable> extends BeanDescriptorImpl<T> {
	
	public BeanEditContext(Class<? extends T> beanClass) {
		super(beanClass);
	}
	
	public BeanEditContext(BeanDescriptor<T> beanDescriptor) {
		super(beanDescriptor);
	}
	
	public abstract Component renderForView(String componentId, IModel<T> model);

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

	public static BeanEditor<Serializable> edit(String componentId, IModel<Serializable> beanModel, boolean selfUpdating) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		BeanEditContext<Serializable> editContext = registry.getBeanEditContext(beanModel.getObject().getClass());
		if (selfUpdating)
			beanModel = editContext.wrapAsSelfUpdating(beanModel);
		return editContext.renderForEdit(componentId, beanModel);
	}
	
	public static BeanEditor<Serializable> edit(String componentId, final Serializable bean) {
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
		return edit(componentId, beanModel, true);
	}

	public static Component view(String componentId, IModel<Serializable> beanModel) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		BeanEditContext<Serializable> editContext = registry.getBeanEditContext(beanModel.getObject().getClass());
		return editContext.renderForView(componentId, beanModel);
	}
	
	public static Component view(String componentId, Serializable bean) {
		return view(componentId, Model.of(bean));
	}
	
	public static BeanEditContext<Serializable> of(Class<? extends Serializable> beanClass) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		return registry.getBeanEditContext(beanClass);
	}
	
	public static BeanEditContext<Serializable> of(BeanDescriptor<? extends Serializable> beanDescriptor) {
		return of(beanDescriptor.getBeanClass());
	}
}
