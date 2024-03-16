package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.SetupCacheFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Editable(order=55, name="Set Up Cache", description = "Set up job cache to speed up job execution. " +
		"Check <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>this tutorial</a> " +
		"to get familiar with job cache")
public class SetupCacheStep extends Step {

	private static final long serialVersionUID = 1L;

	private String key;
	
	private List<String> loadKeys = new ArrayList<>();
	
	private List<String> paths;
	
	private String uploadAccessTokenSecret;

	@Editable(order=100, name="Cache Key", description = "This key is used to determine if there is a cache hit. " +
			"A cache is considered hit if its key is exactly the same as the key defined here")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Editable(order=200, name="Cache Load Keys", description = "In case cache is not hit via above key, OneDev will " +
			"loop through load keys defined here in order until a matching cache is found. A cache is considered " +
			"matching if its key is prefixed with the load key. If multiple caches matches, the most recent cache " +
			"will be returned")
	@Interpolative(variableSuggester="suggestVariables")
	public List<String> getLoadKeys() {
		return loadKeys;
	}

	public void setLoadKeys(List<String> loadKeys) {
		this.loadKeys = loadKeys;
	}
	
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}
	
	@Editable(order=300, name="Cache Paths", description = "For docker aware executors, this path is inside container, " +
			"and accepts both absolute path and relative path (relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>). " +
			"For shell related executors which runs on host machine directly, only relative path is accepted")
	@Interpolative(variableSuggester="suggestStaticVariables")
	@Size(min=1, max=100)
	public List<String> getPaths() {
		return paths;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}

	private static List<InputSuggestion> suggestStaticVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, false, false);
	}
	
	@Editable(order=500, description = "When build is successful, OneDev tries to upload caches not " +
			"hit previously (note matching caches via load keys also considered not hit). This upload " +
			"is permitted only when the build commit is reachable from default branch, or an access " +
			"token is provided with upload cache permission")
	@ChoiceProvider("getAccessTokenSecretChoices")
	public String getUploadAccessTokenSecret() {
		return uploadAccessTokenSecret;
	}

	public void setUploadAccessTokenSecret(String uploadAccessTokenSecret) {
		this.uploadAccessTokenSecret = uploadAccessTokenSecret;
	}

	private static List<String> getAccessTokenSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).distinct().collect(toList());
	}
	
	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		String accessToken;
		if (getUploadAccessTokenSecret() != null)
			accessToken = build.getJobAuthorizationContext().getSecretValue(getUploadAccessTokenSecret());
		else
			accessToken = null;
		return new SetupCacheFacade(key, loadKeys, paths, accessToken);
	}
	
}
 