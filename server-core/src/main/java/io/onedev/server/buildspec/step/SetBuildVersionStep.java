package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.service.BuildService;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.build.BuildUpdated;
import io.onedev.server.persistence.TransactionService;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.onedev.k8shelper.KubernetesHelper.BUILD_VERSION;

@Editable(order=260, name="Set Build Version")
public class SetBuildVersionStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;

	private String buildVersion;

	@Editable(order=100)
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getBuildVersion() {
		return buildVersion;
	}

	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, true, false);
	}
	
	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return OneDev.getInstance(TransactionService.class).call(() -> {
			var build = OneDev.getInstance(BuildService.class).load(buildId);
			build.setVersion(buildVersion);
			OneDev.getInstance(ListenerRegistry.class).post(new BuildUpdated(build));
			Map<String, byte[]> outputFiles = new HashMap<>();
			outputFiles.put(BUILD_VERSION, buildVersion.getBytes(StandardCharsets.UTF_8));
			return new ServerStepResult(true, outputFiles);
		});
		
	}

}
