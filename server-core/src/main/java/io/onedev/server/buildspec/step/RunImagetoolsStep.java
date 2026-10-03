package io.onedev.server.buildspec.step;

import static io.onedev.server.buildspec.step.StepGroup.DOCKER_IMAGE;

import jakarta.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;

@Editable(order=230, name="Run Buildx Image Tools", group = DOCKER_IMAGE, description="Run docker buildx imagetools " +
		"inside a container with specified arguments. This step can be executed by server docker executor, " +
		"remote docker executor, or Kubernetes executor")
public class RunImagetoolsStep extends RegistryToolStep {

	private static final long serialVersionUID = 1L;

	private String arguments;

	@Editable
	@Override
	public String getImage() {
		return "1dev/buildx:1.0.0";
	}

	@Editable(order=100, description="Specify arguments for imagetools. For instance " +
			"<code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>. " +
			"Local OCI layout paths and file options are relative to the job working directory inside the container. " +
			"Host builder configurations are not available")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	@Override
	public String getCommand() {
		return "docker buildx imagetools " + getArguments();
	}

}
