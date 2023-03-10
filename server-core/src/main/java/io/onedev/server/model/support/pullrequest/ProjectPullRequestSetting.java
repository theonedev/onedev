package io.onedev.server.model.support.pullrequest;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.server.annotation.Editable;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

@Editable
public class ProjectPullRequestSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedPullRequestQuery> namedQueries;
	
	private MergeStrategy defaultMergeStrategy;
	
	@Nullable
	public List<NamedPullRequestQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(@Nullable List<NamedPullRequestQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	private static String getLfsDescription() {
		if (!Bootstrap.isInDocker()) {
			return "Whether or not to fetch LFS objects if pull request is opened from a different project. " +
					"If this option is enabled, git lfs command needs to be installed on OneDev server";
		} else {
			return "Whether or not to fetch LFS objects if pull request is opened from a different project.";
		}
	}
	
	@Editable(order=200, placeholder = "Inherit from parent", rootPlaceholder = "Create merge commit", 
			description = "Specify default merge strategy of pull requests submitted to this project")
	public MergeStrategy getDefaultMergeStrategy() {
		return defaultMergeStrategy;
	}

	public void setDefaultMergeStrategy(MergeStrategy defaultMergeStrategy) {
		this.defaultMergeStrategy = defaultMergeStrategy;
	}
}
