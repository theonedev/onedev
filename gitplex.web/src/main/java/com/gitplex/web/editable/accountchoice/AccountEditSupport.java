package com.gitplex.web.editable.accountchoice;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.util.StringUtils;

import com.gitplex.core.annotation.AccountChoice;
import com.gitplex.commons.wicket.editable.BeanContext;
import com.gitplex.commons.wicket.editable.EditSupport;
import com.gitplex.commons.wicket.editable.EditableUtils;
import com.gitplex.commons.wicket.editable.NotDefinedLabel;
import com.gitplex.commons.wicket.editable.PropertyContext;
import com.gitplex.commons.wicket.editable.PropertyDescriptor;
import com.gitplex.commons.wicket.editable.PropertyEditor;
import com.gitplex.commons.wicket.editable.PropertyViewer;

@SuppressWarnings("serial")
public class AccountEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass, Set<String> excludeProperties) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);
        Method propertyGetter = propertyDescriptor.getPropertyGetter();
        AccountChoice accountChoice = propertyGetter.getAnnotation(AccountChoice.class);
        if (accountChoice != null) {
        	if (List.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& EditableUtils.getElementClass(propertyGetter.getGenericReturnType()) == String.class) {
        		return new PropertyContext<List<String>>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        List<String> userNames = model.getObject();
						        if (userNames != null && !userNames.isEmpty()) {
						            return new Label(id, StringUtils.join(userNames, ", " ));
						        } else {
									return new NotDefinedLabel(id);
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
						return new AccountMultiChoiceEditor(componentId, this, model, accountChoice.organization());
					}
        			
        		};
        	} else if (propertyGetter.getReturnType() == String.class) {
        		return new PropertyContext<String>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<String> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null) {
						            return new Label(id, model.getObject());
						        } else {
									return new NotDefinedLabel(id);
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
						return new AccountSingleChoiceEditor(componentId, this, model, accountChoice.organization());
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'UserChoice' should be applied to property with type 'String' or 'List<String>'.");
        	}
        } else {
            return null;
        }
	}

}
