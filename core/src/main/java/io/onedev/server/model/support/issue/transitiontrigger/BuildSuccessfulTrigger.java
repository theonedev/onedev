package io.onedev.server.model.support.issue.transitiontrigger;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.Usage;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.BranchPatterns;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.JobChoice;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.TagPatterns;

@Editable(order=400, name="Build fixing the issue is successful")
public class BuildSuccessfulTrigger implements TransitionTrigger {

	private static final long serialVersionUID = 1L;
	
	private String jobName;
	
	private String branches;
	
	private String tags;
	
	@Editable(order=100, description="Specify job of the build")
	@JobChoice
	@NotEmpty
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Editable(order=200, name="Restricted Branches", description="Optionally specify branches the build should be "
			+ "running on. Use space to separate multipe branches, and use * or ? for wildcard match")
	@BranchPatterns
	@NameOfEmptyValue("No restriction")
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}
	
	@Editable(order=300, name="Restricted Tags", description="Optionally specify tags the build should be running on. "
			+ "Use space to separate multipe tags, and use * or ? for wildcard match")
	@TagPatterns
	@NameOfEmptyValue("No restriction")
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	
	public Usage onDeleteBranch(String branchName) {
		Usage usage = new Usage();
		PatternSet patternSet = PatternSet.fromString(getBranches());
		if (patternSet.getIncludes().contains(branchName) || patternSet.getExcludes().contains(branchName))
			usage.add("restricted branches");
		return usage;
	}
	
	public Usage onDeleteTag(String tagName) {
		Usage usage = new Usage();
		PatternSet patternSet = PatternSet.fromString(getTags());
		if (patternSet.getIncludes().contains(tagName) || patternSet.getExcludes().contains(tagName))
			usage.add("restricted tags");
		return usage;
	}
	
}
