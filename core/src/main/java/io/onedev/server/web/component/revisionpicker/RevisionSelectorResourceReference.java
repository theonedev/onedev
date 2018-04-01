package io.onedev.server.web.component.revisionpicker;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.asset.hotkeys.HotkeysResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class RevisionSelectorResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public RevisionSelectorResourceReference() {
		super(RevisionSelectorResourceReference.class, "revision-selector.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(RevisionSelectorResourceReference.class, "revision-selector.css")));
		return dependencies;
	}

}
