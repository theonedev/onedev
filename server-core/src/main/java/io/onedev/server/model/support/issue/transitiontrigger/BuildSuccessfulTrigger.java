package io.onedev.server.model.support.issue.transitiontrigger;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.Usage;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.BranchPatterns;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.JobChoice;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(order=400, name="Build fixing the issue is successful")
public class BuildSuccessfulTrigger implements TransitionTrigger {

	private static final long serialVersionUID = 1L;
	
	private String jobName;
	
	private String branches;
	
	@Editable(order=100, description="Specify job of the build")
	@JobChoice
	@NotEmpty
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Editable(order=200, name="Applicable Branches", description="Optionally specify space-separated branches "
			+ "applicable for this trigger. Use * or ? for wildcard match")
	@BranchPatterns
	@NameOfEmptyValue("All")
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}
	
	public Usage onDeleteBranch(String branchName) {
		Usage usage = new Usage();
		PatternSet patternSet = PatternSet.fromString(getBranches());
		if (patternSet.getIncludes().contains(branchName) || patternSet.getExcludes().contains(branchName))
			usage.add("applicable branches");
		return usage;
	}
	
}
