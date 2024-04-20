package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.*;
import io.onedev.server.buildspec.BuildSpec;

import javax.validation.constraints.NotEmpty;
import java.util.List;

import static io.onedev.server.buildspec.step.StepGroup.DOCKER_IMAGE;

@Editable(order=240, name="Pull Docker Image", group = DOCKER_IMAGE, description="Pull docker image as OCI layout via crane. " +
		"This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor")
public class PullImageStep extends CraneStep {

	private static final long serialVersionUID = 1L;
	
	private String srcImage;
	
	private String destPath;
	
	private String platform;
	
	private String moreOptions;
	
	@Editable(order=100, name="Source Docker Image", description="Specify full tag of source docker image to pull from, " +
			"for instance <tt>registry-server/org/repo:tag</tt>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getSrcImage() {
		return srcImage;
	}

	public void setSrcImage(String srcImage) {
		this.srcImage = srcImage;
	}

	@Editable(order=200, name="OCI Layout Directory", description = "Specify directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout")
	@Interpolative(variableSuggester="suggestVariables")
	@SubPath
	@NoSpace
	@NotEmpty
	public String getDestPath() {
		return destPath;
	}

	public void setDestPath(String destPath) {
		this.destPath = destPath;
	}

	@Editable(order=1100, group = "More Settings", placeholder = "All platforms in image", description = "" +
			"Optionally specify platform to pull, for instance <tt>linux/amd64</tt>. " +
			"Leave empty to pull all platforms in image")
	@Interpolative(variableSuggester="suggestVariables")
	@NoSpace
	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	@Editable(order=1200, group="More Settings", description="Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>additional options</a> of crane")
	@Interpolative(variableSuggester="suggestVariables")
	@ReservedOptions({"--format", "(--format)=.*", "--platform", "(--platform)=.*"})
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
	public String getCommand() {
		var builder = new StringBuilder("crane pull --format oci ");
		if (getPlatform() != null)
			builder.append("--platform ").append(getPlatform()).append(" ");
		if (getMoreOptions() != null)
			builder.append(getMoreOptions()).append(" ");
		builder.append(getSrcImage()).append(" /onedev-build/workspace/").append(getDestPath());
		return builder.toString();
	}
	
}
