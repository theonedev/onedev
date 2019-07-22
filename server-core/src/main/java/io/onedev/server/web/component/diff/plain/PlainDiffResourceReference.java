package io.onedev.server.web.component.diff.plain;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.component.diff.table.DiffTableResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class PlainDiffResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public PlainDiffResourceReference() {
		super(PlainDiffResourceReference.class, "plain-diff.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new DiffTableResourceReference()));
		return dependencies;
	}

}
