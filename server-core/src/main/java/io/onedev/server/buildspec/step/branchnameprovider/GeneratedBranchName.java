package io.onedev.server.buildspec.step.branchnameprovider;

import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.model.Build;
import io.onedev.server.service.IssueService;

@Editable(order=100, name="Use generated branch name", description="""
        Generate branch name based on issue title. It is highly recommended to configure 
		AI model in <i>Administration / AI Settings</i> to generate good branch name""")
public class GeneratedBranchName implements BranchNameProvider {

	private static final long serialVersionUID = 1L;

	private String prefix;

	@Editable(order=100, name="Branch Prefix", placeholder = "No prefix", description="Optionally specify a prefix to be prepended to generated branch name")
	@Interpolative(variableSuggester="suggestVariables")
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String getBranchName(Build build) {
		if (build.getIssue() != null) {
			var branchName = OneDev.getInstance(IssueService.class).suggestBranch(build.getIssue());
			if (prefix != null)
				branchName = prefix + branchName;
			return branchName;
		} else {
			throw new ExplicitException("Generated branch name is only available when issue state triggers are used");
		}
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

}
