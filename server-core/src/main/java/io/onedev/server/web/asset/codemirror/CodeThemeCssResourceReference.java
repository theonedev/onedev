package io.onedev.server.web.asset.codemirror;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

public class CodeThemeCssResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public CodeThemeCssResourceReference() {
		// must use CodeMirrorResourceReference.class as resource scope to 
		// make sure it is only loaded once
		super(CodeMirrorResourceReference.class, "theme-custom.css");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		
		// must use CodeMirrorResourceReference.class as resource scope to 
		// make sure it is only loaded once
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				CodeMirrorResourceReference.class, "lib/codemirror.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				CodeMirrorResourceReference.class, "theme/eclipse.css")));
		return dependencies;
	}

}
