package io.onedev.server.buildspec.job;

import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Build;
import io.onedev.server.util.UrlUtils;

import java.net.MalformedURLException;
import java.net.URL;

import static io.onedev.k8shelper.KubernetesHelper.PLACEHOLDER_PREFIX;
import static io.onedev.k8shelper.KubernetesHelper.PLACEHOLDER_SUFFIX;

public enum JobVariable {

	PROJECT_NAME {

		@Override
		public String getValue(Build build) {
			return build.getProject().getName();
		}

	}, 
	PROJECT_PATH {

		@Override
		public String getValue(Build build) {
			return build.getProject().getPath();
		}

	}, 
	JOB_NAME {

		@Override
		public String getValue(Build build) {
			return build.getJobName();
		}
		
	}, 
	JOB_TOKEN {
		@Override
		public String getValue(Build build) {
			return build.getJobToken();
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
			if (build.getVersion() != null)
				return build.getVersion();
			else
				return PLACEHOLDER_PREFIX + KubernetesHelper.BUILD_VERSION + PLACEHOLDER_SUFFIX;
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
		
	},
	ISSUE_NUMBER {
		@Override
		public String getValue(Build build) {
			if (build.getIssue() != null)
				return String.valueOf(build.getIssue().getNumber());
			else
				return null;
		}
	},
	SERVER {
		@Override
		public String getValue(Build build) {
			var serverUrl = OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
			return UrlUtils.getServer(serverUrl);
		}
	},
	SERVER_HOST {
		@Override
		public String getValue(Build build) {
			var serverUrl = OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
			try {
				return new URL(serverUrl).getHost();
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	},
	SERVER_URL {
		@Override
		public String getValue(Build build) {
			return OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
		}
	};

	public abstract String getValue(Build build);
	
}
