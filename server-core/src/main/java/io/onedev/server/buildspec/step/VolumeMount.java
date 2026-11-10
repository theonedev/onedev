package io.onedev.server.buildspec.step;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.VolumeMountFacade;
import io.onedev.server.annotation.Path;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;

@Editable
public class VolumeMount implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String source;
	
	private String target;

	private boolean readOnly = true;

	@Editable(order = 100, placeholder = "Job workdir", description = "Specify a path relative to job working directory " +
			"to be used as mount source. Leave empty to mount job working directory itself")
	@Interpolative(variableSuggester="suggestVariables")
	@Path(Path.Type.RELATIVE)
	public String getSourcePath() {
		return source;
	}

	public void setSourcePath(String sourcePath) {
		this.source = sourcePath;
	}

	@Editable(order=200, description="Specify a path inside container to be used as mount target")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getTargetPath() {
		return target;
	}

	public void setTargetPath(String targetPath) {
		this.target = targetPath;
	}

	@Editable(order=300, description="Mount this path read-only at the specified container target")
	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public VolumeMountFacade getFacade() {
		var sourcePath = getSourcePath();
		return new VolumeMountFacade(sourcePath != null ? sourcePath : ".", getTargetPath(), isReadOnly());
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}
	
}
