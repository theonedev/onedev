package io.onedev.server.web.component.logging;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.joblogentry.JobLogEntryResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class LogResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public LogResourceReference() {
		super(LogResourceReference.class, "log.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JobLogEntryResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				LogResourceReference.class, "log.css")));
		return dependencies;
	}

}
