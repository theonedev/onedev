package io.onedev.server.buildspec.step.branchnameprovider;

import java.io.Serial;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.Build;
import io.onedev.server.service.IssueService;

@Editable(order=100, name="Use generated branch name", description="""
        Generate branch name based on issue title. It is highly recommended to configure 
		AI model in <i>Administration / AI Settings</i> to generate good branch name""")
public class GeneratedBranchName implements BranchNameProvider {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public String getBranchName(Build build) {
		if (build.getIssue() != null) {
			return OneDev.getInstance(IssueService.class).suggestBranch(build.getIssue());
		} else {
			throw new ExplicitException("Generated branch name is only available when issue state triggers are used");
		}
	}

}
