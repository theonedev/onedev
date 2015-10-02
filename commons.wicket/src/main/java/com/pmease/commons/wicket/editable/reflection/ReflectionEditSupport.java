package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanDescriptor;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.BeanViewer;
import com.pmease.commons.wicket.editable.DefaultPropertyDescriptor;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.NotDefinedLabel;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.commons.wicket.editable.PropertyViewer;
import com.pmease.commons.wicket.editable.annotation.Editable;

@SuppressWarnings("serial")
public class ReflectionEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass) {
		if (beanClass.getAnnotation(Editable.class) != null && ClassUtils.isConcrete(beanClass)) {
			return new BeanContext<Serializable>(beanClass) {

				@Override
				public BeanViewer renderForView(String componentId, final IModel<Serializable> model) {
					return new BeanViewer(componentId, this) {

						@Override
						protected Component newContent(String id, BeanDescriptor beanDescriptor) {
							return new ReflectionBeanViewer(id, beanDescriptor, model);
						}
						
					};
				}

				@Override
				public BeanEditor<Serializable> renderForEdit(String componentId, IModel<Serializable> model) {
					return new ReflectionBeanEditor(componentId, this, model);
				}
				
			};
		} else {
			return null;
		}
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new DefaultPropertyDescriptor(beanClass, propertyName);
		Class<?> propertyClass = propertyDescriptor.getPropertyClass();
		if (propertyClass.getAnnotation(Editable.class) != null && ClassUtils.isConcrete(propertyClass)) {
			return new PropertyContext<Serializable>(propertyDescriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<Serializable> model) {
					return new PropertyViewer(componentId, this) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								return BeanContext.viewBean(id, model.getObject());
							} else {
								return new NotDefinedLabel(id);
							}
						}
						
					};
				}

				@Override
				public PropertyEditor<Serializable> renderForEdit(String componentId, IModel<Serializable> model) {
					return new ReflectionPropertyEditor(componentId, this, model);
				}
				
			};
		} else {
			return null;
		}
	}

}
