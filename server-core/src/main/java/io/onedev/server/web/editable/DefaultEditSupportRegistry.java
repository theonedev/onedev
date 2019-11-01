package io.onedev.server.web.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.emory.mathcs.backport.java.util.Collections;

@Singleton
public class DefaultEditSupportRegistry implements EditSupportRegistry {

	private final List<EditSupport> editSupports;
	
	@Inject
	public DefaultEditSupportRegistry(Set<EditSupport> editSupports) {
		this.editSupports = new ArrayList<EditSupport>(editSupports);
		Collections.sort(this.editSupports, new Comparator<EditSupport>() {

			@Override
			public int compare(EditSupport o1, EditSupport o2) {
				return o1.getPriority() - o2.getPriority();
			}
			
		});
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
