package com.pmease.commons.wicket.editor;

import java.io.Serializable;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public BeanEditContext<Serializable> getBeanEditContext(Class<? extends Serializable> beanClass) {
		for (EditSupport each: editSupports) {
			BeanEditContext<?> editContext = each.getBeanEditContext(beanClass);
			if (editContext != null)
				return (BeanEditContext<Serializable>) editContext;
		}
		throw new GeneralException(String.format("Unable to find edit context (bean: %s)", beanClass.getName()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public PropertyEditContext<Serializable> getPropertyEditContext(Class<? extends Serializable> beanClass, String propertyName) {
		for (EditSupport each: editSupports) {
			PropertyEditContext<?> editContext = each.getPropertyEditContext(beanClass, propertyName);
			if (editContext != null)
				return (PropertyEditContext<Serializable>) editContext;
		}
		throw new GeneralException(String.format(
				"Unable to find edit context (bean: %s, property: %s)", 
				beanClass.getName(), propertyName));
	}

}
