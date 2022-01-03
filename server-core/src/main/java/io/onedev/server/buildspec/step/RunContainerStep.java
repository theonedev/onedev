package io.onedev.server.buildspec.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.ContainerExecutable;
import io.onedev.k8shelper.Executable;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.EnvVar;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=150, name="Run Container")
public class RunContainerStep extends Step {

	private static final long serialVersionUID = 1L;

	private String image;
	
	private String args;
	
	private List<EnvVar> envVars = new ArrayList<>();

	private String workingDir;
	
	private boolean useTTY;
	
	@Override
	public Executable getExecutable(Build build, String jobToken, ParamCombination paramCombination) {
		Map<String, String> envMap = new HashMap<>();
		for (EnvVar var: getEnvVars())
			envMap.put(var.getName(), var.getValue());
		return new ContainerExecutable(getImage(), getArgs(), envMap, getWorkingDir(), isUseTTY());
	}

	@Editable(order=100, description="Specify container image to run. <b class='text-warning'>NOTE:</b> A shell must "
			+ "exist in the container if the step is executed by kubernetes executor, as OneDev intercepts the "
			+ "entrypoint to make step containers executing sequentially in the pod")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order=200, name="Arguments", description="Specify container arguments separated by space. "
			+ "Single argument containing space should be quoted")
	@Interpolative(variableSuggester="suggestVariables")
	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	@Editable(order=200, name="Working Directory", description="Optionally specify working directory of the container. "
			+ "Leave empty to use the default working directory. "
			+ "<a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>Job workspace</a> will be "
			+ "mounted to the working directory so that the container can access job files")
	@Nullable
	public String getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	@Editable(order=400, name="Environment Variables", description="Optionally specify environment "
			+ "variables for the container")
	public List<EnvVar> getEnvVars() {
		return envVars;
	}

	public void setEnvVars(List<EnvVar> envVars) {
		this.envVars = envVars;
	}

	@Editable(order=10000, name="Enable TTY Mode", description="Many commands print outputs with ANSI colors in "
			+ "TTY mode to help identifying problems easily. However some commands running in this mode may "
			+ "wait for user input to cause build hanging. This can normally be fixed by adding extra options "
			+ "to the command")
	public boolean isUseTTY() {
		return useTTY;
	}

	public void setUseTTY(boolean useTTY) {
		this.useTTY = useTTY;
	}

	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false);
	}
	
}
