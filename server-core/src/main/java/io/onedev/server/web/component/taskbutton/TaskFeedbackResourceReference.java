package io.onedev.server.web.component.taskbutton;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.joblogentry.JobLogEntryResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class TaskFeedbackResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public TaskFeedbackResourceReference() {
		super(TaskFeedbackResourceReference.class, "task-feedback.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JobLogEntryResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				TaskFeedbackResourceReference.class, "task-feedback.css")));
		return dependencies;
	}

}
