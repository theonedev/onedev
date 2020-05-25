package io.onedev.server.buildspec.job;

import java.util.stream.Collectors;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.git.GitUtils;
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
	UPDATED_REF {

		@Override
		public String getValue(Build build) {
			return build.getUpdatedRef();
		}
		
	},
	UPDATED_BRANCH {

		@Override
		public String getValue(Build build) {
			if (build.getUpdatedRef() != null)
				return GitUtils.ref2branch(build.getUpdatedRef());
			else
				return null;
		}
		
	},
	UPDATED_TAG {

		@Override
		public String getValue(Build build) {
			if (build.getUpdatedRef() != null)
				return GitUtils.ref2tag(build.getUpdatedRef());
			else
				return null;
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
	BUILD_VERSION {

		@Override
		public String getValue(Build build) {
			return build.getVersion();
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
			return build.getVerifications()
					.stream()
					.map(it->it.getRequest().getId().toString())
					.collect(Collectors.joining(" "));
		}
		
	};
	
	public abstract String getValue(Build build);
	
}
