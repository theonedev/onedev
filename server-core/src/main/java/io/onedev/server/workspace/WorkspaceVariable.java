package io.onedev.server.workspace;

import java.net.MalformedURLException;
import java.net.URL;

import io.onedev.server.OneDev;
import io.onedev.server.model.Workspace;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.UrlUtils;

public enum WorkspaceVariable {

	PROJECT_NAME {

		@Override
		public String getValue(Workspace workspace) {
			return workspace.getProject().getName();
		}

	}, 
	PROJECT_PATH {

		@Override
		public String getValue(Workspace workspace) {
			return workspace.getProject().getPath();
		}

	}, 
	SPEC_NAME {

		@Override
		public String getValue(Workspace workspace) {
			return workspace.getSpecName();
		}
		
	}, 
	WORKSPACE_TOKEN {
		@Override
		public String getValue(Workspace workspace) {
			return workspace.getToken();
		}
	},
	BRANCH {

		@Override
		public String getValue(Workspace workspace) {
			return workspace.getBranch();
		}
		
	},
	WORKSPACE_NUMBER {

		@Override
		public String getValue(Workspace workspace) {
			return String.valueOf(workspace.getNumber());
		}
		
	}, 
	SERVER {
		@Override
		public String getValue(Workspace workspace) {
			var serverUrl = OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
			return UrlUtils.getServer(serverUrl);
		}
	},
	SERVER_HOST {
		@Override
		public String getValue(Workspace workspace) {
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
		public String getValue(Workspace workspace) {
			return OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
		}
	};

	public abstract String getValue(Workspace workspace);

}
