package io.onedev.server.web.asset.joblogentry;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.moment.MomentResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class JobLogEntryResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public JobLogEntryResourceReference() {
		super(JobLogEntryResourceReference.class, "job-log-entry.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new MomentResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				JobLogEntryResourceReference.class, "job-log-entry.css")));
		return dependencies;
	}

}
