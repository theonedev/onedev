package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.RunContainerFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.SafePath;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.EnvVar;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Editable(order=150, name="Run Docker Container", description="Run specified docker container. To access files in "
		+ "job workspace, either use environment variable <tt>ONEDEV_WORKSPACE</tt>, or specify volume mounts. " +
		"<b class='text-warning'>Note: </b> this step can only be executed by server docker executor or remote " +
		"docker executor")
public class RunContainerStep extends Step {

	private static final long serialVersionUID = 1L;

	private String image;
	
	private String args;
	
	private List<EnvVar> envVars = new ArrayList<>();

	private String workingDir;
	
	private List<VolumeMount> volumeMounts = new ArrayList<>(); 
	
	private boolean useTTY;
	
	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		Map<String, String> envMap = new HashMap<>();
		for (EnvVar var: getEnvVars())
			envMap.put(var.getName(), var.getValue());
		Map<String, String> mountMap = new HashMap<>();
		for (VolumeMount mount: getVolumeMounts()) {
			var sourcePath = mount.getSourcePath();
			if (sourcePath == null)
				sourcePath = ".";
			mountMap.put(sourcePath, mount.getTargetPath());
		}
		return new RunContainerFacade(getImage(), null, getArgs(), envMap, getWorkingDir(), mountMap, isUseTTY());
	}

	@Editable(order=100, description="Specify container image to run")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order=200, name="Arguments", description="Optionally specify container arguments separated by space. " +
			"Single argument containing space should be quoted. <b class='text-warning'>Note: </b> do not confuse " +
			"this with container options which should be specified in executor setting")
	@Interpolative(variableSuggester="suggestVariables")
	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	@Editable(order=300, name="Working Directory", description="Optionally specify working directory of the container. "
			+ "Leave empty to use default working directory of the container")
	@SafePath
	@Interpolative(variableSuggester="suggestVariables")
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

	@Editable(order=500, description="Optionally mount directories or files under job workspace into container")
	public List<VolumeMount> getVolumeMounts() {
		return volumeMounts;
	}

	public void setVolumeMounts(List<VolumeMount> volumeMounts) {
		this.volumeMounts = volumeMounts;
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
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
}
