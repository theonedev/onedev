package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Markdown;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.service.BuildService;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.build.BuildUpdated;
import io.onedev.server.persistence.TransactionService;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.List;

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
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger jobLogger) {
		return OneDev.getInstance(TransactionService.class).call(() -> {
			var build = OneDev.getInstance(BuildService.class).load(buildId);
			build.setDescription(buildDescription);
			OneDev.getInstance(ListenerRegistry.class).post(new BuildUpdated(build));
			return new ServerStepResult(true);
		});
		
	}

}
