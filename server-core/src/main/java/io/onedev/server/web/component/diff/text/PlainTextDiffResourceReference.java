package io.onedev.server.web.component.diff.text;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.textdiff.TextDiffResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class PlainTextDiffResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public PlainTextDiffResourceReference() {
		super(PlainTextDiffResourceReference.class, "plain-text-diff.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new TextDiffResourceReference()));
		return dependencies;
	}

}

