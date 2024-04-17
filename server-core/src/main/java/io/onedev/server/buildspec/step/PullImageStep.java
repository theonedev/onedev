package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.ReservedOptions;
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
	
	private String moreOptions;
	
	@Editable(order=100, name="Source Docker Image", description="Specify source docker image to pull from")
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
	@NotEmpty
	public String getDestPath() {
		return destPath;
	}

	public void setDestPath(String destPath) {
		this.destPath = destPath;
	}

	@Editable(order=1200, group="More Settings", description="Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>additional options</a> of crane")
	@Interpolative(variableSuggester="suggestVariables")
	@ReservedOptions({"(--format)=.*"})
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
		return "crane pull --format oci " + getSrcImage() + " /onedev-build/workspace/" + getDestPath();
	}
	
}
