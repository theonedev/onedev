package io.onedev.server.web.editable;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hibernate.proxy.HibernateProxyHelper;

import com.google.common.collect.Sets;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.web.editable.annotation.Editable;

@SuppressWarnings("serial")
public class BeanContext implements Serializable {
	
	private final BeanDescriptor descriptor;
	
	public BeanContext(Class<?> beanClass) {
		descriptor = new BeanDescriptor(beanClass, Sets.newHashSet(), true);
	}
	
	public BeanContext(Class<?> beanClass, Collection<String> properties, boolean excluded) {
		descriptor = new BeanDescriptor(beanClass, properties, excluded);
	}
	
	public BeanContext(BeanDescriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	public BeanDescriptor getDescriptor() {
		return descriptor;
	}
	
	public BeanViewer renderForView(String componentId, IModel<Serializable> model) {
		checkBeanEditable();
		return new BeanViewer(componentId, descriptor, model);
	}

	public BeanEditor renderForEdit(String componentId, IModel<Serializable> model) {
		checkBeanEditable();
		return new BeanEditor(componentId, descriptor, model);
	}
	
	private void checkBeanEditable() {
		Class<?> beanClass = descriptor.getBeanClass();
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
				descriptor.copyProperties(object, getObject());
			}
			
		};
	}

	public static BeanEditor editModel(String componentId, IModel<? extends Serializable> beanModel) {
		return editModel(componentId, beanModel, Sets.newHashSet(), true);
	}
	
	@SuppressWarnings("unchecked")
	public static BeanEditor editModel(String componentId, 
			IModel<? extends Serializable> beanModel, Collection<String> properties, boolean excluded) {
		Class<?> beanClass = HibernateProxyHelper.getClassWithoutInitializingProxy(beanModel.getObject());
		BeanContext beanContext = new BeanContext(beanClass, properties, excluded);
		return beanContext.renderForEdit(componentId, (IModel<Serializable>)beanModel);
	}
	
	public static BeanEditor edit(String componentId, Serializable bean) {
		return edit(componentId, bean, Sets.newHashSet(), true);
	}
	
	public static BeanEditor edit(String componentId, Serializable bean, Collection<String> properties, boolean excluded) {
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
		Class<?> beanClass = HibernateProxyHelper.getClassWithoutInitializingProxy(beanModel.getObject());
		BeanContext beanContext = new BeanContext(beanClass, properties, excluded);
		beanModel = beanContext.wrapAsSelfUpdating(beanModel);
		return beanContext.renderForEdit(componentId, beanModel);
	}

	public static Component viewModel(String componentId, IModel<Serializable> beanModel) {
		return viewModel(componentId, beanModel, new HashSet<>(), true);
	}
			
	public static Component viewModel(String componentId, IModel<Serializable> beanModel, 
			Set<String> properties, boolean excluded) {
		Class<?> beanClass = HibernateProxyHelper.getClassWithoutInitializingProxy(beanModel.getObject());
		BeanContext editContext = new BeanContext(beanClass, properties, excluded);
		return editContext.renderForView(componentId, beanModel);
	}
	
	public static Component view(String componentId, Serializable bean, Set<String> properties, boolean excluded) {
		return viewModel(componentId, Model.of(bean), properties, excluded);
	}
	
	public static Component view(String componentId, Serializable bean) {
		return view(componentId, bean, new HashSet<>(), true);
	}
	
}
