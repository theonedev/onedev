package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
		Collections.sort(this.editSupports, new Comparator<EditSupport>() {

			@Override
			public int compare(EditSupport editSupport1, EditSupport editSupport2) {
				return editSupport2.getPriorty() - editSupport1.getPriorty();
			}
			
		});
	}
	
	@Override
	public BeanEditContext getBeanEditContext(Serializable bean) {
		for (EditSupport each: editSupports) {
			BeanEditContext editContext = each.getBeanEditContext(bean);
			if (editContext != null)
				return editContext;
		}
		throw new GeneralException("Unable to find edit context (bean: %s)", bean);
	}

	@Override
	public PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName) {
		for (EditSupport each: editSupports) {
			PropertyEditContext editContext = each.getPropertyEditContext(bean, propertyName);
			if (editContext != null)
				return editContext;
		}
		throw new GeneralException(
				"Unable to find edit context (bean: %s, property: %s)", 
				bean, propertyName);
	}

}
