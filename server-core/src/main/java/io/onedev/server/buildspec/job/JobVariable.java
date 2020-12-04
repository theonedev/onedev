package io.onedev.server.buildspec.job;

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
	REF {

		@Override
		public String getValue(Build build) {
			return build.getRefName();
		}
		
	},
	BRANCH {

		@Override
		public String getValue(Build build) {
			return build.getBranch();
		}
		
	},
	TAG {

		@Override
		public String getValue(Build build) {
			return build.getTag();
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
	PULL_REQUEST_NUMBER {

		@Override
		public String getValue(Build build) {
			if (build.getRequest() != null)
				return String.valueOf(build.getRequest().getNumber());
			else
				return null;
		}
		
	}; 
	
	public abstract String getValue(Build build);
	
}
