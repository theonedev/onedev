package io.onedev.server.web.asset.mermaid;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.onedev.server.web.page.base.BaseDependentResourceReference;
import io.onedev.server.web.resourcebundle.ResourceBundle;

@ResourceBundle
public class MermaidResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public MermaidResourceReference() {
		super(MermaidResourceReference.class, "mermaid-init.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				MermaidResourceReference.class, "mermaid.min.js")));
		return dependencies;
	}

}
