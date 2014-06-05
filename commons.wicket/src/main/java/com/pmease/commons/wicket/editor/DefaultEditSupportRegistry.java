package com.pmease.commons.wicket.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.util.GeneralException;

@Singleton
public class DefaultEditSupportRegistry implements EditSupportRegistry {

	private final List<EditSupport> editSupports;
	
	@Inject
	public DefaultEditSupportRegistry(Set<EditSupport> editSupports) {
		this.editSupports = new ArrayList<EditSupport>(editSupports);
	}
	
	@Override
	public BeanEditContext<Object> getBeanEditContext(Class<?> beanClass) {
		for (EditSupport each: editSupports) {
			BeanEditContext<Object> editContext = each.getBeanEditContext(beanClass);
			if (editContext != null)
				return editContext;
		}
		throw new GeneralException(String.format("Unable to find edit context (bean: %s)", beanClass.getName()));
	}

	@Override
	public PropertyEditContext<Object> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		for (EditSupport each: editSupports) {
			PropertyEditContext<Object> editContext = each.getPropertyEditContext(beanClass, propertyName);
			if (editContext != null)
				return editContext;
		}
		throw new GeneralException(String.format(
				"Unable to find edit context (bean: %s, property: %s)", 
				beanClass.getName(), propertyName));
	}

}
