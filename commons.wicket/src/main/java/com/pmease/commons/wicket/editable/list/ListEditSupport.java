package com.pmease.commons.wicket.editable.list;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.editable.PropertyDescriptorImpl;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.NotDefinedLabel;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.commons.wicket.editable.PropertyViewer;
import com.pmease.commons.wicket.editable.list.polymorphic.PolymorphicListPropertyEditor;
import com.pmease.commons.wicket.editable.list.polymorphic.PolymorphicListPropertyViewer;
import com.pmease.commons.wicket.editable.list.table.TableListPropertyEditor;
import com.pmease.commons.wicket.editable.list.table.TableListPropertyViewer;

@SuppressWarnings("serial")
public class ListEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptorImpl(beanClass, propertyName);
		
		if (List.class.isAssignableFrom(propertyDescriptor.getPropertyClass())) {
			final Class<?> elementClass = EditableUtils.getElementClass(propertyDescriptor.getPropertyGetter().getGenericReturnType());
			if (elementClass != null) {
				if (ClassUtils.isConcrete(elementClass)) {
					if (elementClass.getAnnotation(Editable.class) != null) {
						return new PropertyContext<List<Serializable>>(propertyDescriptor) {

							@Override
							public PropertyViewer renderForView(String componentId, final IModel<List<Serializable>> model) {
								return new PropertyViewer(componentId, this) {

									@Override
									protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
										if (model.getObject() != null) {
											return new TableListPropertyViewer(id, elementClass, model.getObject());
										} else {
											return new NotDefinedLabel(id);
										}
									}
									
								};
							}

							@Override
							public PropertyEditor<List<Serializable>> renderForEdit(String componentId, IModel<List<Serializable>> model) {
								return new TableListPropertyEditor(componentId, this, model);
							}
							
						};
					}
				} else if (elementClass.getAnnotation(Editable.class) != null) {
					return new PropertyContext<List<Serializable>>(propertyDescriptor) {

						@Override
						public PropertyViewer renderForView(String componentId, final IModel<List<Serializable>> model) {
							return new PropertyViewer(componentId, this) {

								@Override
								protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
									if (model.getObject() != null) {
										return new PolymorphicListPropertyViewer(id, model.getObject());
									} else {
										return new NotDefinedLabel(id);
									}
								}
								
							};
						}

						@Override
						public PropertyEditor<List<Serializable>> renderForEdit(String componentId, IModel<List<Serializable>> model) {
							return new PolymorphicListPropertyEditor(componentId, this, model);
						}
						
					};
				}
			}
		}
		return null;
	}

}
