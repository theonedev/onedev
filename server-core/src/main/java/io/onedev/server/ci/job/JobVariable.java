package io.onedev.server.ci.job;

import java.util.stream.Collectors;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Build;

public enum JobVariable {

	PROJECT_NAME {

		@Override
		public String getValue(Build build) {
			return build.getProject().getName();
		}

	}, 
	JOB_NAME {

		@Override
		public String getValue(Build build) {
			return build.getJobName();
		}
		
	}, 
	COMMIT_HASH {

		@Override
		public String getValue(Build build) {
			return build.getCommitHash();
		}
		
	}, 
	BUILD_NUMBER {

		@Override
		public String getValue(Build build) {
			return String.valueOf(build.getNumber());
		}
		
	}, 
	ON_BRANCHES {
		
		@Override
		public String getValue(Build build) {
			return StringUtils.join(build.getOnBranches(), " ");
		}
		
	}, 
	PULL_REQUEST_IDS {

		@Override
		public String getValue(Build build) {
			return build.getPullRequestBuilds()
					.stream()
					.map(it->it.getRequest().getId().toString())
					.collect(Collectors.joining(" "));
		}
		
	};
	
	public abstract String getValue(Build build);
	
}
