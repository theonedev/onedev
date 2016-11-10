package com.gitplex.commons.wicket.editable.list;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.gitplex.commons.util.ClassUtils;
import com.gitplex.commons.wicket.editable.BeanContext;
import com.gitplex.commons.wicket.editable.EditSupport;
import com.gitplex.commons.wicket.editable.EditableUtils;
import com.gitplex.commons.wicket.editable.NotDefinedLabel;
import com.gitplex.commons.wicket.editable.PropertyContext;
import com.gitplex.commons.wicket.editable.PropertyDescriptor;
import com.gitplex.commons.wicket.editable.PropertyEditor;
import com.gitplex.commons.wicket.editable.PropertyViewer;
import com.gitplex.commons.wicket.editable.annotation.ChoiceProvider;
import com.gitplex.commons.wicket.editable.annotation.Editable;
import com.gitplex.commons.wicket.editable.list.concrete.ConcreteListPropertyEditor;
import com.gitplex.commons.wicket.editable.list.concrete.ConcreteListPropertyViewer;
import com.gitplex.commons.wicket.editable.list.polymorphic.PolymorphicListPropertyEditor;
import com.gitplex.commons.wicket.editable.list.polymorphic.PolymorphicListPropertyViewer;

@SuppressWarnings("serial")
public class ListEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass, Set<String> excludeProperties) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);
		
		if (List.class.isAssignableFrom(propertyDescriptor.getPropertyClass())) {
			final Class<?> elementClass = EditableUtils.getElementClass(propertyDescriptor.getPropertyGetter().getGenericReturnType());
			if (elementClass != null) {
				if (Enum.class.isAssignableFrom(elementClass)) {
		            return new PropertyContext<List<Enum<?>>>(propertyDescriptor) {

						@Override
						public PropertyViewer renderForView(String componentId, final IModel<List<Enum<?>>> model) {

							return new PropertyViewer(componentId, this) {

								@Override
								protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							        if (model.getObject() != null && !model.getObject().isEmpty()) {
							            String content = "";
							            for (Enum<?> each: model.getObject()) {
							            	if (content.length() == 0)
							            		content += each.toString();
							            	else
							            		content += ", " + each.toString();
							            }
							            return new Label(id, content);
							        } else { 
										return new NotDefinedLabel(id);
							        }
								}
								
							};
						}

						@Override
						public PropertyEditor<List<Enum<?>>> renderForEdit(String componentId, IModel<List<Enum<?>>> model) {
							return new EnumListPropertyEditor(componentId, this, model);
						}
		            	
		            };
				} else if (elementClass == String.class 
						&& propertyDescriptor.getPropertyGetter().getAnnotation(ChoiceProvider.class) != null) {
		            return new PropertyContext<List<String>>(propertyDescriptor) {

						@Override
						public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {

							return new PropertyViewer(componentId, this) {

								@Override
								protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							        if (model.getObject() != null && !model.getObject().isEmpty()) {
							            String content = "";
							            for (String each: model.getObject()) {
							            	if (content.length() == 0)
							            		content += each.toString();
							            	else
							            		content += ", " + each.toString();
							            }
							            return new Label(id, content);
							        } else { 
										return new NotDefinedLabel(id);
							        }
								}
								
							};
						}

						@Override
						public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
							return new StringListPropertyEditor(componentId, this, model);
						}
		            	
		            };
				} else if (ClassUtils.isConcrete(elementClass)) {
					if (elementClass.getAnnotation(Editable.class) != null) {
						return new PropertyContext<List<Serializable>>(propertyDescriptor) {

							@Override
							public PropertyViewer renderForView(String componentId, final IModel<List<Serializable>> model) {
								return new PropertyViewer(componentId, this) {

									@Override
									protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
										if (model.getObject() != null) {
											return new ConcreteListPropertyViewer(id, elementClass, model.getObject());
										} else {
											return new NotDefinedLabel(id);
										}
									}
									
								};
							}

							@Override
							public PropertyEditor<List<Serializable>> renderForEdit(String componentId, IModel<List<Serializable>> model) {
								return new ConcreteListPropertyEditor(componentId, this, model);
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
										return new PolymorphicListPropertyViewer(id, propertyDescriptor, model.getObject());
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
