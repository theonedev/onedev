package io.onedev.server.buildspec.step;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.BuildImageFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.util.validation.annotation.SafePath;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=160, name="Build Docker Image", description="Build and optionally publish docker image. "
		+ "<span class='text-danger'>Registry logins should be specified</span> in the job executor executing this step if registry authentication "
		+ "is required for build or publish")
public class BuildImageStep extends Step {

	private static final long serialVersionUID = 1L;

	private String buildPath;
	
	private String dockerfile;
	
	private String tags;
	
	private boolean publish;
	
	@Editable(order=100, description="Optionally specify build path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. "
			+ "Leave empty to use job workspace itself")
	@Interpolative(variableSuggester="suggestVariables")
	@SafePath
	public String getBuildPath() {
		return buildPath;
	}

	public void setBuildPath(String buildPath) {
		this.buildPath = buildPath;
	}

	@Editable(order=200, description="Optionally specify Dockerfile relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. "
			+ "Leave empty to use file <tt>Dockerfile</tt> under build path specified above")
	@Interpolative(variableSuggester="suggestVariables")
	@SafePath
	public String getDockerfile() {
		return dockerfile;
	}

	public void setDockerfile(String dockerfile) {
		this.dockerfile = dockerfile;
	}

	@Editable(order=300, description="Specify full tag of the image, for instance <tt>myorg/myrepo:latest</tt>, "
			+ "<tt>myorg/myrepo:1.0.0</tt>, or <tt>myregistry:5000/myorg/myrepo:1.0.0</tt>. "
			+ "Multiple tags should be separated with space.<br>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@Editable(order=400, name="Publish After Build", description="Whether or not to publish built image to docker registry")
	public boolean isPublish() {
		return publish;
	}

	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}
	
	@Override
	public StepFacade getFacade(Build build, String jobToken, ParamCombination paramCombination) {
		return new BuildImageFacade(getBuildPath(), getDockerfile(), getTags(), isPublish());
	}

}
