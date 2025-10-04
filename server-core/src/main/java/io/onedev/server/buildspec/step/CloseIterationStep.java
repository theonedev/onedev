package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.IterationService;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionService;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Editable(name="Close Iteration", order=400)
public class CloseIterationStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;
	
	private String iterationName;
	
	private String accessTokenSecret;
	
	@Editable(order=1000, description="Specify name of the iteration")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getIterationName() {
		return iterationName;
	}

	public void setIterationName(String iterationName) {
		this.iterationName = iterationName;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	@Editable(order=1060, description="For build commit not reachable from default branch, " +
			"a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with manage issue permission")
	@ChoiceProvider("getAccessTokenSecretChoices")
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}

	@SuppressWarnings("unused")
	private static List<String> getAccessTokenSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return OneDev.getInstance(TransactionService.class).call(() -> {
			var build = OneDev.getInstance(BuildService.class).load(buildId);
			Project project = build.getProject();
			String iterationName = getIterationName();
			IterationService iterationService = OneDev.getInstance(IterationService.class);
			Iteration iteration = iterationService.findInHierarchy(project, iterationName);
			if (iteration != null) {
				if (build.canCloseIteration(getAccessTokenSecret())) {
					iteration.setClosed(true);
					iterationService.createOrUpdate(iteration);
				} else {
					logger.error("This build is not authorized to close iteration '" + iterationName + "'");
					return new ServerStepResult(false);
				}
			} else {
				logger.warning("Unable to find iteration '" + iterationName + "' to close. Ignored.");
			}
			return new ServerStepResult(true);
		});
	}

}
