package io.onedev.server.buildspec.job;

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
	REF {

		@Override
		public String getValue(Build build) {
			return build.getRefName();
		}
		
	},
	BRANCH {

		@Override
		public String getValue(Build build) {
			if (build.getRefName() != null)
				return GitUtils.ref2branch(build.getRefName());
			else
				return null;
		}
		
	},
	TAG {

		@Override
		public String getValue(Build build) {
			if (build.getRefName() != null)
				return GitUtils.ref2tag(build.getRefName());
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
		
	}; 
	
	public abstract String getValue(Build build);
	
}
