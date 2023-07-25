package io.onedev.server.buildspec.step;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.BuildImageFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.annotation.ReservedOptions;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.annotation.SafePath;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

@Editable(order=160, name="Build Docker Image", description="Build and publish docker image with docker daemon. " +
		"This step can only be executed by server docker executor or remote docker executor, and " +
		"<code>mount docker sock</code> option needs to be enabled on the executor. To build image with " +
		"Kubernetes executor, please use kaniko step. <b class='text-danger'>NOTE: </b> registry logins " +
		"should be configured in the job executor if authentication is required for build or publish")
public class BuildImageStep extends Step {

	private static final long serialVersionUID = 1L;

	private String buildPath;
	
	private String dockerfile;
	
	private String tags;

	private boolean publish = true;
	
	private boolean removeDanglingImages = true;
	
	private String moreOptions;
	
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

	@Editable(order=330, name="Publish After Build", description="Whether or not to publish built image to docker registry")
	public boolean isPublish() {
		return publish;
	}

	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	@Editable(order=340, name="Remove Dangling Images After Build")
	public boolean isRemoveDanglingImages() {
		return removeDanglingImages;
	}

	public void setRemoveDanglingImages(boolean removeDanglingImages) {
		this.removeDanglingImages = removeDanglingImages;
	}

	@Editable(order=350, description="Optionally specify additional options to build image, " +
			"separated by spaces")
	@Interpolative(variableSuggester="suggestVariables")
	@ReservedOptions({"-f", "(--file)=.*", "-t", "(--tag)=.*"})
	public String getMoreOptions() {
		return moreOptions;
	}

	public void setMoreOptions(String moreOptions) {
		this.moreOptions = moreOptions;
	}


	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}
	
	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		return new BuildImageFacade(getBuildPath(), getDockerfile(), getTags(), isPublish(), isRemoveDanglingImages(), getMoreOptions());
	}

}
