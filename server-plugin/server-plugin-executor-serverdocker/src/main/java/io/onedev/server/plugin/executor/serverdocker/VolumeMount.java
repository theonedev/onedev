package io.onedev.server.plugin.executor.serverdocker;

import java.io.Serial;
import java.io.Serializable;

import io.onedev.k8shelper.VolumeMountFacade;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Path;
import jakarta.validation.constraints.NotEmpty;

@Editable
public class VolumeMount implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String sourcePath;

	private String targetPath;

	private boolean readOnly = true;

	@Editable(order=100, description="File or directory on the server/agent filesystem. "
			+ "Keep the source, any symlink targets, and their parent directories under administrator control. "
			+ "If the server/agent runs in Docker, bind mount it into the server/agent first and specify the path visible inside it")
	@NotEmpty
	@Path(Path.Type.ABSOLUTE)
	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	@Editable(order=200, description="Absolute path in job containers")
	@NotEmpty
	@Path(Path.Type.ABSOLUTE)
	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	@Editable(order=300, description="Prevent jobs from modifying the mounted file or directory")
	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public VolumeMountFacade getFacade() {
		return new VolumeMountFacade(sourcePath, targetPath, readOnly);
	}

}
