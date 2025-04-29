package io.onedev.server.web.asset.tippy;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class TippyResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public TippyResourceReference() {
		super(TippyResourceReference.class, "tippy.min.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(TippyResourceReference.class, "popper.min.js")));
		return dependencies;
	}
	
}
