package com.pmease.commons.wicket.editor;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.editable.BeanDescriptor;
import com.pmease.commons.editable.BeanDescriptorImpl;
import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
public abstract class BeanEditContext<T> extends BeanDescriptorImpl<T> {
	
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

	public static BeanEditor<Object> edit(String componentId, IModel<Object> beanModel, boolean selfUpdating) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		BeanEditContext<Object> editContext = registry.getBeanEditContext(beanModel.getObject().getClass());
		if (selfUpdating)
			beanModel = editContext.wrapAsSelfUpdating(beanModel);
		return editContext.renderForEdit(componentId, beanModel);
	}
	
	public static BeanEditor<Object> edit(String componentId, final Serializable bean) {
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
		return edit(componentId, beanModel);
	}

	public static Component view(String componentId, IModel<Object> beanModel) {
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		BeanEditContext<Object> editContext = registry.getBeanEditContext(beanModel.getObject().getClass());
		return editContext.renderForView(componentId, beanModel);
	}
	
	public static Component view(String componentId, Serializable bean) {
		return view(componentId, Model.of(bean));
	}
	
}
