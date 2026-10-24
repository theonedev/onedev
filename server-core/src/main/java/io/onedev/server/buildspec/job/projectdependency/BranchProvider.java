package io.onedev.server.buildspec.job.projectdependency;

import java.io.Serial;

import org.eclipse.jgit.lib.Constants;

import io.onedev.server.annotation.Editable;
import jakarta.validation.constraints.NotEmpty;

@Editable(order=100, name="Branch")
public class BranchProvider implements RefNameProvider {

	@Serial
	private static final long serialVersionUID = 1L;

	private String branchPattern;

	@Editable(description="Specify branch name. Use * for wildcard match")
	@NotEmpty
	public String getBranchPattern() {
		return branchPattern;
	}

	public void setBranchPattern(String branchPattern) {
		this.branchPattern = branchPattern;
	}

	@Override
	public String getRefName() {
		return Constants.R_HEADS + branchPattern;
	}

}
