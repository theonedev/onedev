package io.onedev.server.buildspec.step;

import static io.onedev.k8shelper.KubernetesHelper.BUILD_VERSION;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.event.build.BuildUpdated;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=20, name="Set Build Version")
public class SetBuildVersionStep extends ServerStep {

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
		return BuildSpec.suggestVariables(matchWith, false, true);
	}
	
	@Override
	public Map<String, byte[]> run(Build build, File filesDir, TaskLogger jobLogger) {
		return OneDev.getInstance(TransactionManager.class).call(new Callable<Map<String, byte[]>>() {

			@Override
			public Map<String, byte[]> call() {
				build.setVersion(buildVersion);
				OneDev.getInstance(ListenerRegistry.class).post(new BuildUpdated(build));
				Map<String, byte[]> outputFiles = new HashMap<>();
				outputFiles.put(BUILD_VERSION, buildVersion.getBytes(StandardCharsets.UTF_8));
				return outputFiles;
			}
			
		});
		
	}

}
