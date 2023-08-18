package io.onedev.server.ee.subscription;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import java.util.List;

public class SupportRequestResourceReference extends BaseDependentCssResourceReference {
	
	public SupportRequestResourceReference() {
		super(SupportRequestResourceReference.class, "support-request.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		var dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				SupportRequestResourceReference.class, "support-request.css")));
		return dependencies;
	}
}
