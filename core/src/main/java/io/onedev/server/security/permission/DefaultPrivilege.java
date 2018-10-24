package io.onedev.server.security.permission;

public enum DefaultPrivilege {
	ISSUE_READ {

		@Override
		public ProjectPrivilege getProjectPrivilege() {
			return ProjectPrivilege.ISSUE_READ;
		}
		
	},
	CODE_READ {

		@Override
		public ProjectPrivilege getProjectPrivilege() {
			return ProjectPrivilege.CODE_READ;
		}

	};

	public abstract ProjectPrivilege getProjectPrivilege();
	
}
