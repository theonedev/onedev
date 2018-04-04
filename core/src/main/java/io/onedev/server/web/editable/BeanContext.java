package io.onedev.server.web.editable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Sets;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.utils.ClassUtils;

@SuppressWarnings("serial")
public class BeanContext extends BeanDescriptor {
	
	public BeanContext(Class<?> beanClass) {
		this(beanClass, Sets.newHashSet());
	}
	
	public BeanContext(Class<?> beanClass, Set<String> excludedProperties) {
		super(beanClass, excludedProperties);
	}
	
	public BeanContext(BeanDescriptor beanDescriptor) {
		super(beanDescriptor);
	}
	
	public BeanViewer renderForView(String componentId, IModel<Serializable> model) {
		checkBeanEditable();
		return new BeanViewer(componentId, this, model);
	}

	public BeanEditor renderForEdit(String componentId, IModel<Serializable> model) {
		checkBeanEditable();
		return new BeanEditor(componentId, this, model);
	}
	
	private void checkBeanEditable() {
		Class<?> beanClass = getBeanClass();
		if (beanClass.getAnnotation(Editable.class) == null) {
			throw new RuntimeException("Can not edit bean " + beanClass 
				+ " as it is not annotated with @Editable");
		} else if (!ClassUtils.isConcrete(beanClass)) {
			throw new RuntimeException("Can not edit bean " + beanClass 
				+ " as it is not concrete");
		}
		
	}
	
	public IModel<Serializable> wrapAsSelfUpdating(final IModel<Serializable> model) {
		return new IModel<Serializable>() {

			@Override
			public void detach() {
				model.detach();
			}

			@Override
			public Serializable getObject() {
				return model.getObject();
			}

			@Override
			public void setObject(Serializable object) {
				copyProperties(object, getObject());
			}
			
		};
	}

	public static BeanEditor editModel(String componentId, IModel<? extends Serializable> beanModel) {
		return editModel(componentId, beanModel, Sets.newHashSet());
	}
	
	@SuppressWarnings("unchecked")
	public static BeanEditor editModel(String componentId, 
			IModel<? extends Serializable> beanModel, Set<String> excludedProperties) {
		Class<?> beanClass = ClassUtils.unproxy(beanModel.getObject().getClass());
		BeanContext beanContext = new BeanContext(beanClass, excludedProperties);
		return beanContext.renderForEdit(componentId, (IModel<Serializable>)beanModel);
	}
	
	public static BeanEditor editBean(String componentId, Serializable bean) {
		return editBean(componentId, bean, Sets.newHashSet());
	}
	
	public static BeanEditor editBean(String componentId, final Serializable bean, 
			Set<String> excludedProperties) {
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
		Class<?> beanClass = ClassUtils.unproxy(beanModel.getObject().getClass());
		BeanContext beanContext = new BeanContext(beanClass, excludedProperties);
		beanModel = beanContext.wrapAsSelfUpdating(beanModel);
		return beanContext.renderForEdit(componentId, beanModel);
	}

	public static Component viewModel(String componentId, IModel<Serializable> beanModel) {
		return viewModel(componentId, beanModel, new HashSet<>());
	}
			
	public static Component viewModel(String componentId, IModel<Serializable> beanModel, 
			Set<String> excludedProperties) {
		BeanContext editContext = new BeanContext(beanModel.getObject().getClass(), excludedProperties);
		return editContext.renderForView(componentId, beanModel);
	}
	
	public static Component viewBean(String componentId, Serializable bean, Set<String> excludedProperties) {
		return viewModel(componentId, Model.of(bean), excludedProperties);
	}
	
	public static Component viewBean(String componentId, Serializable bean) {
		return viewBean(componentId, bean, new HashSet<>());
	}
	
}
