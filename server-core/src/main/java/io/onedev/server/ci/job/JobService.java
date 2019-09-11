package io.onedev.server.ci.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.validation.annotation.VariableName;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class JobService implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String image;
	
	private String arguments;
	
	private List<Variable> envVars = new ArrayList<>();
	
	private String readinessCheckCommand;
	
	private String cpuRequirement = "500m";
	
	private String memoryRequirement = "128m";
	
	@Editable(order=100, description="Specify name of the service, which can be used to access "
			+ "the service")
	@VariableName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="Specify docker image of the service")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order=220, description="Optionally specify arguments to run above image")
	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	@Editable(order=300, name="Environment Variables", description="Optionally specify environment variables of "
			+ "the service")
	public List<Variable> getEnvVars() {
		return envVars;
	}

	public void setEnvVars(List<Variable> envVars) {
		this.envVars = envVars;
	}

	@Editable(order=400, description="Specify command to check readiness of the service. This command will "
			+ "be interpretated by cmd.exe on Windows images, and by shell on Linux images. It will be "
			+ "executed repeatedly until a zero code is returned, which means that service is ready")
	@NotEmpty
	public String getReadinessCheckCommand() {
		return readinessCheckCommand;
	}

	public void setReadinessCheckCommand(String readinessCheckCommand) {
		this.readinessCheckCommand = readinessCheckCommand;
	}
	
	@Editable(order=10000, name="CPU Requirement", group="More Settings", description="Specify CPU requirement of the job. "
			+ "Refer to <a href='https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#meaning-of-cpu' target='_blank'>kubernetes documentation</a> for details")
	@NotEmpty
	public String getCpuRequirement() {
		return cpuRequirement;
	}

	public void setCpuRequirement(String cpuRequirement) {
		this.cpuRequirement = cpuRequirement;
	}

	@Editable(order=10100, group="More Settings", description="Specify memory requirement of the job. "
			+ "Refer to <a href='https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#meaning-of-memory' target='_blank'>kubernetes documentation</a> for details")
	@NotEmpty
	public String getMemoryRequirement() {
		return memoryRequirement;
	}

	public void setMemoryRequirement(String memoryRequirement) {
		this.memoryRequirement = memoryRequirement;
	}
	
}
