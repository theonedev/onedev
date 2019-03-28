package io.onedev.server.ci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.ci.jobtrigger.JobTrigger;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.PathPatterns;

@Editable
@Horizontal
public class Job implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String image;
	
	private String command;
	
	private boolean cloneSource = true;
	
	private String publishArtifacts;

	private List<Dependency> dependencies = new ArrayList<>();
	
	private List<InputSpec> promptParams = new ArrayList<>();
	
	private List<JobTrigger> triggers = new ArrayList<>();
	
	private long timeout = 3600;
	
	@Editable(order=100, description="Specify name of the job")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=110, name="Docker Image", description="Specify the docker image to run the command")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order=120)
	@Multiline
	@NotEmpty
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Editable(order=130, description="Whether or not to clone the source code")
	public boolean isCloneSource() {
		return cloneSource;
	}

	public void setCloneSource(boolean cloneSource) {
		this.cloneSource = cloneSource;
	}	
	
	@Editable(order=300, description="Specify parameters to prompt when the job "
			+ "is triggered manually")
	public List<InputSpec> getPromptParams() {
		return promptParams;
	}

	public void setPromptParams(List<InputSpec> promptParams) {
		this.promptParams = promptParams;
	}

	@Editable(name="Dependency Jobs", order=400, description="Job dependencies determines the order and "
			+ "concurrency when run different jobs")
	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
	}

	@Editable(order=500, description="Use triggers to run the job automatically under certain conditions")
	public List<JobTrigger> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<JobTrigger> triggers) {
		this.triggers = triggers;
	}

	@Editable(order=600, description="Optionally specify space-separated workspace files to publish as artifacts. "
			+ "Use * or ? for wildcard match")
	@PathPatterns
	public String getPublishArtifacts() {
		return publishArtifacts;
	}

	public void setPublishArtifacts(String publishArtifacts) {
		this.publishArtifacts = publishArtifacts;
	}

	@Editable(order=700, description="Specify timeout in seconds")
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public JobTrigger getMatchedTrigger(ProjectEvent event) {
		for (JobTrigger trigger: getTriggers()) {
			if (trigger.matches(event, this))
				return trigger;
		}
		return null;
	}
	
}
