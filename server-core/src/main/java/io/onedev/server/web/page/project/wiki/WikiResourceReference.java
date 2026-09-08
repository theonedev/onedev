package io.onedev.server.web.page.project.wiki;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.component.markdown.MarkdownResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class WikiResourceReference extends BaseDependentResourceReference {

	public WikiResourceReference() {
		super(WikiResourceReference.class, "wiki.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		var dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new MarkdownResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				WikiResourceReference.class, "wiki.css")));
		return dependencies;
	}
}
