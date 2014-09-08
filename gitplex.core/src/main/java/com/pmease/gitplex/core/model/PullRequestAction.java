package com.pmease.gitplex.core.model;

import java.io.Serializable;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
public interface PullRequestAction extends Serializable {

	public static class Integrate implements PullRequestAction {
		
		private final String reason;
		
		public Integrate(@Nullable String reason) {
			this.reason = reason;
		}

		@Nullable
		public String getReason() {
			return reason;
		}
		
	}
	
	public static class Discard implements PullRequestAction {
		
	}
	
	public static class Approve implements PullRequestAction {
		
	}
	
	public static class Disapprove implements PullRequestAction {
		
	}
	
}
