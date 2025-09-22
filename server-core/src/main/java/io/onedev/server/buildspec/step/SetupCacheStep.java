package io.onedev.server.buildspec.step;

import static io.onedev.k8shelper.SetupCacheFacade.UploadStrategy.UPLOAD_IF_NOT_HIT;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.SetupCacheFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

@Editable(order=55, name="Set Up Cache", description = "Set up job cache to speed up job execution. " +
		"Check <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>this tutorial</a> " +
		"on how to use job cache")
public class SetupCacheStep extends Step {

	private static final long serialVersionUID = 1L;
	
	private String key;
	
	private List<String> loadKeys = new ArrayList<>();
	
	private List<String> paths;
	
	private SetupCacheFacade.UploadStrategy uploadStrategy = UPLOAD_IF_NOT_HIT;
	
	private String changeDetectionExcludes;
	
	private String uploadProjectPath;
	
	private String uploadAccessTokenSecret;

	@Editable(order=100, name="Cache Key", description = "This key is used to determine if there is a cache hit in " +
			"project hierarchy (search from current project to root project in order, same for load keys below). " +
			"A cache is considered hit if its key is exactly the same as the key defined here.<br>" + 
			"<b>NOTE:</b> In case your project has lock files(package.json, pom.xml, etc.) able to represent cache state, " + 
			"this key should be defined as &lt;cache name&gt;-@file:checksum.txt@, where checksum.txt is generated " + 
			"from these lock files with the <b>generate checksum step</b> defined before this step")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Editable(order=200, name="Load Keys", description = "In case cache is not hit via above key, " +
			"OneDev will loop through load keys defined here in order until a matching cache is found " +
			"in project hierarchy. A cache is considered matching if its key is prefixed with the load " +
			"key. If multiple caches matches, the most recent cache will be returned")
	@Interpolative(variableSuggester="suggestVariables")
	public List<String> getLoadKeys() {
		return loadKeys;
	}

	public void setLoadKeys(List<String> loadKeys) {
		this.loadKeys = loadKeys;
	}
		
	@Editable(order=300, name="Cache Paths", description = "For docker aware executors, this path is inside container, " +
			"and accepts both absolute path and relative path (relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>). " +
			"For shell related executors which runs on host machine directly, only relative path is accepted")
	@Interpolative(variableSuggester="suggestVariables")
	@Size(min=1, max=100)
	public List<String> getPaths() {
		return paths;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}

	@Editable(order=400, description = "Specify cache upload strategy after build successful. " +
			"<var>Upload If Not Hit</var> means to upload when cache is not found with " +
			"cache key (not load keys), and <var>Upload If Changed</var> means to upload if some files " +
			"in cache path are changed")
	@NotNull
	public SetupCacheFacade.UploadStrategy getUploadStrategy() {
		return uploadStrategy;
	}

	public void setUploadStrategy(SetupCacheFacade.UploadStrategy uploadStrategy) {
		this.uploadStrategy = uploadStrategy;
	}

	@Editable(order=425, description = "Optionally specify files relative to cache path to ignore when " +
			"detect cache changes. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. " +
			"Multiple files should be separated by space, and single file containing space should be quoted")
	@Interpolative(variableSuggester="suggestVariables")
	@DependsOn(property="uploadStrategy", value="UPLOAD_IF_CHANGED")
	public String getChangeDetectionExcludes() {
		return changeDetectionExcludes;
	}
	
	public void setChangeDetectionExcludes(String changeDetectionExcludes) {
		this.changeDetectionExcludes = changeDetectionExcludes;
	}
	
	@Editable(order=450, name="Upload to Project", placeholder = "Current project", description = "In case cache needs to be uploaded, this property " +
			"specifies target project for the upload. Leave empty for current project")
	@ProjectChoice
	public String getUploadProjectPath() {
		return uploadProjectPath;
	}

	public void setUploadProjectPath(String uploadProjectPath) {
		this.uploadProjectPath = uploadProjectPath;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}
	
	@Editable(order=500, description = "Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with upload cache permission " +
			"for above project. Note that this property is not required if upload cache to current or child project " +
			"and build commit is reachable from default branch")
	@ChoiceProvider("getAccessTokenSecretChoices")
	public String getUploadAccessTokenSecret() {
		return uploadAccessTokenSecret;
	}

	public void setUploadAccessTokenSecret(String uploadAccessTokenSecret) {
		this.uploadAccessTokenSecret = uploadAccessTokenSecret;
	}

	@SuppressWarnings("unused")
	private static List<String> getAccessTokenSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).distinct().collect(toList());
	}
	
	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		String uploadAccessToken;
		if (getUploadAccessTokenSecret() != null)
			uploadAccessToken = build.getJobAuthorizationContext().getSecretValue(getUploadAccessTokenSecret());
		else
			uploadAccessToken = null;
		return new SetupCacheFacade(getKey(), getLoadKeys(), getPaths(), getUploadStrategy(), 
				getChangeDetectionExcludes(), getUploadProjectPath(), uploadAccessToken);
	}
	
	@Override
	public boolean isApplicable(Build build, JobExecutor executor) {
		return true;
	}

}
 