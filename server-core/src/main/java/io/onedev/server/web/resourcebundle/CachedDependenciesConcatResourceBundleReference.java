package io.onedev.server.web.resourcebundle;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IReferenceHeaderItem;
import org.apache.wicket.resource.bundles.ConcatResourceBundleReference;

@SuppressWarnings("serial")
public class CachedDependenciesConcatResourceBundleReference<T extends HeaderItem & IReferenceHeaderItem> 
		extends ConcatResourceBundleReference<T> {

	private List<HeaderItem> dependencies;
	
	public CachedDependenciesConcatResourceBundleReference(Class<?> scope, String name, List<T> resources) {
		super(scope, name, resources);
	}

	@Override
	public List<HeaderItem> getDependencies() {
		if (dependencies == null) {
			synchronized (this) {
				if (dependencies == null)
					dependencies = super.getDependencies();
			}
		}
		return dependencies;
	}

}
