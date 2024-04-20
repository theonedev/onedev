package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.*;
import io.onedev.server.buildspec.BuildSpec;

import javax.validation.constraints.NotEmpty;
import java.util.List;

import static io.onedev.server.buildspec.step.StepGroup.DOCKER_IMAGE;

@Editable(order=250, name="Push Docker Image", group = DOCKER_IMAGE, description="Push docker image from OCI layout via crane. " +
		"This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor")
public class PushImageStep extends CraneStep {

	private static final long serialVersionUID = 1L;
	
	private String srcPath;
	
	private String destImage;
	
	private String moreOptions;
	
	@Editable(order=100, name="OCI Layout Directory", description = "Specify OCI layout directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to push from")
	@Interpolative(variableSuggester="suggestVariables")
	@SubPath
	@NoSpace
	@NotEmpty
	public String getSrcPath() {
		return srcPath;
	}

	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}

	@Editable(order=200, name="Target Docker Image", description="Specify full tag of target docker image to push to, " +
			"for instance <tt>registry-server/org/repo:tag</tt>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getDestImage() {
		return destImage;
	}

	public void setDestImage(String destImage) {
		this.destImage = destImage;
	}

	@Editable(order=1200, group="More Settings", description="Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>additional options</a> of crane")
	@ReservedOptions({"--platform", "(--platform)=.*"})
	@Interpolative(variableSuggester="suggestVariables")
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
		var builder = new StringBuilder("crane push");
		if (getMoreOptions() != null)
			builder.append(" ").append(getMoreOptions());			
		builder.append(" /onedev-build/workspace/").append(getSrcPath()).append(" ").append(getDestImage());
		return builder.toString();
	}
	
}
