package io.onedev.server.web.page.project.builds.detail.log;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.joblogentry.JobLogEntryResourceReference;
import io.onedev.server.web.component.build.status.BuildStatusCssResourceReference;
import io.onedev.server.web.component.progress.TimeProgressResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class BuildStepsResourceReference extends BaseDependentResourceReference {
	private static final long serialVersionUID = 1L;

	public BuildStepsResourceReference() {
		super(BuildStepsResourceReference.class, "build-steps.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		var dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JobLogEntryResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new TimeProgressResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BuildStatusCssResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				BuildStepsResourceReference.class, "build-steps.css")));
		return dependencies;
	}
}
