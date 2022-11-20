package io.onedev.server.buildspec.step;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.build.BuildUpdated;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Markdown;

@Editable(order=265, name="Set Build Description")
public class SetBuildDescriptionStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;

	private String buildDescription;

	@Editable(order=100)
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	@Markdown
	public String getBuildDescription() {
		return buildDescription;
	}

	public void setBuildDescription(String buildDescription) {
		this.buildDescription = buildDescription;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, true, false);
	}
	
	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger jobLogger) {
		return OneDev.getInstance(TransactionManager.class).call(new Callable<Map<String, byte[]>>() {

			@Override
			public Map<String, byte[]> call() {
				build.setDescription(buildDescription);
				OneDev.getInstance(ListenerRegistry.class).post(new BuildUpdated(build));
				return null;
			}
			
		});
		
	}

}
