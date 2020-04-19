package io.onedev.server.web.page.project.compare;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.asset.revisioncompare.RevisionCompareCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class RevisionCompareResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public RevisionCompareResourceReference() {
		super(RevisionCompareResourceReference.class, "revision-compare.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new RevisionCompareCssResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				RevisionCompareResourceReference.class, "revision-compare.css")));
		return dependencies;
	}

}
