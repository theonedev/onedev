package io.onedev.server.web.page.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class BaseDependentResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public BaseDependentResourceReference(Class<?> scope, String name) {
		super(scope, name);
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseResourceReference()));
		return dependencies;
	}

}
