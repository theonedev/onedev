package io.onedev.server.buildspec.step.branchnameprovider;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.BranchName;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.model.Build;

@Editable(order=200, name="Use specified branch name")
public class SpecifiedBranchName implements BranchNameProvider {

	private static final long serialVersionUID = 1L;

	private String name;

	@Editable(order=100, description="Specify name of the branch")
	@Interpolative(variableSuggester="suggestVariables")
	@BranchName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getBranchName(Build build) {
		return getName();
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

}
