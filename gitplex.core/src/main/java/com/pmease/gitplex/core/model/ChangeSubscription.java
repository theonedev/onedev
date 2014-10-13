package com.pmease.gitplex.core.model;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Horizontal;
import com.pmease.gitplex.core.branchmatcher.GlobalBranchMatcher;

@SuppressWarnings("serial")
@Editable
@Horizontal
public class ChangeSubscription implements Serializable {

	public enum Source {
		COMMIT("Notify about commits made to specified branch and path"), 
		PULL_REQUEST("Notify about pull requests submitted to specified branch and path"), 
		BOTH("Notify about both commits and pull requests");
		
		private final String description;
		
		private Source(String description) {
			this.description = description;
		}

		@Override
		public String toString() {
			return description;
		}
		
	};
	
	private GlobalBranchMatcher branches;
	
	private String paths;
	
	private Source source;
	
	@Editable(name="Branches", order=100, description="Specify branches to get change notifications about, "
			+ "including pull requests, commits, and relevant action/comments.")
	@NotNull
	@Valid
	public GlobalBranchMatcher getBranches() {
		return branches;
	}

	public void setBranches(GlobalBranchMatcher branches) {
		this.branches = branches;
	}

	@Editable(name="Path Patterns", order=200, description="Optionally specify "
			+ "<a href='http://wiki.pmease.com/display/gp/Pattern+Set'>path patterns</a> to filter change "
			+ "notifications. For instance, Specifying this field as <em>module1/**</em> tells GitPlex to "
			+ "only send notificiations if the change (pull request or commit) touches any file under "
			+ "<em>module1</em>. If not specified, notification will be sent for all changes of specified "
			+ "branches.")
	public String getPaths() {
		return paths;
	}

	public void setPaths(String paths) {
		this.paths = paths;
	}

	@Editable(name="Notification Source", order=300)
	@NotNull
	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}
	
}
