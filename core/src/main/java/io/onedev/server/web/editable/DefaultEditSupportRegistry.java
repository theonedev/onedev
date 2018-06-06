package io.onedev.server.web.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultEditSupportRegistry implements EditSupportRegistry {

	private final List<EditSupport> editSupports;
	
	@Inject
	public DefaultEditSupportRegistry(Set<EditSupport> editSupports) {
		this.editSupports = new ArrayList<EditSupport>(editSupports);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PropertyContext<Serializable> getPropertyEditContext(PropertyDescriptor descriptor) {
		for (EditSupport each: editSupports) {
			PropertyContext<?> editContext = each.getEditContext(descriptor);
			if (editContext != null)
				return (PropertyContext<Serializable>) editContext;
		}
		throw new RuntimeException(String.format(
				"Unable to find edit context (bean: %s, property: %s). Possible reason: forget to annotate "
				+ "return type of the method with @Editable", 
				descriptor.getBeanClass().getName(), descriptor.getPropertyName()));
	}

}
