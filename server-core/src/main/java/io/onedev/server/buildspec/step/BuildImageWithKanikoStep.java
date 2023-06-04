package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.RunContainerFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.ReservedOptions;
import io.onedev.server.annotation.SafePath;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;

@Editable(order=200, name="Build Docker Image (Kaniko)", description="Build and publish docker image with Kaniko. " +
		"This step can be executed by server docker executor, remote docker executor, or Kubernetes executor, " +
		"without the need to mount docker sock. <b class='text-danger'>NOTE: </b> registry logins should be " +
		"configured in the job executor if authentication is required for image build or publish")
public class BuildImageWithKanikoStep extends Step {

	private static final long serialVersionUID = 1L;

	private String buildContext;
	
	private String destinations;

	private String moreOptions;
	
	@Editable(order=100, description="Optionally specify build context path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. "
			+ "Leave empty to use job workspace itself. The file <code>Dockerfile</code> is expected to exist in build context " +
			"directory, unless you specify a different location with option <code>--dockerfile</code>")
	@Interpolative(variableSuggester="suggestVariables")
	@SafePath
	public String getBuildContext() {
		return buildContext;
	}

	public void setBuildContext(String buildContext) {
		this.buildContext = buildContext;
	}

	@Editable(order=300, description="Specify destinations, for instance <tt>myorg/myrepo:latest</tt>, "
			+ "<tt>myorg/myrepo:1.0.0</tt>, or <tt>myregistry:5000/myorg/myrepo:1.0.0</tt>. "
			+ "Multiple destinations should be separated with space.<br>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getDestinations() {
		return destinations;
	}

	public void setDestinations(String destinations) {
		this.destinations = destinations;
	}
	
	@Editable(order=350, description="Optionally specify additional options to build image, " +
			"separated by spaces")
	@Interpolative(variableSuggester="suggestVariables")
	@ReservedOptions({"(--context)=.*", "(--destination)=.*"})
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
	public StepFacade getFacade(Build build, String jobToken, ParamCombination paramCombination) {
		var args = new StringBuilder();
		var volumeMounts = new HashMap<String, String>();
		if (getBuildContext() != null) {
			volumeMounts.put(getBuildContext(), "/onedev-build/kaniko");
			args.append("--context=/onedev-build/kaniko ");
		} else {
			args.append("--context=/onedev-build/workspace ");
		}
		for (var destination: StringUtils.splitAndTrim(getDestinations(), " ")) 
			args.append("--destination=").append(destination).append(" ");
		if (getMoreOptions() != null)
			args.append(getMoreOptions());
		
		return new RunContainerFacade("1dev/kaniko:latest", 
				args.toString(), new HashMap<>(), null, volumeMounts, true, true);
	}

}
