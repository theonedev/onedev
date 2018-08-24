package io.onedev.server.security.permission;

public enum ProjectPrivilege {

	ReadCode("Read code") {

		@Override
		public boolean implies(ProjectPrivilege privilege) {
			return privilege == ReadCode;
		}
		
	},
	WriteCode("Write code") {

		@Override
		public boolean implies(ProjectPrivilege privilege) {
			return privilege == WriteCode || ReadCode.implies(privilege);
		}
		
	},
	ReadIssues("Read issues") {

		@Override
		public boolean implies(ProjectPrivilege privilege) {
			return privilege == ReadIssues;
		}
		
	},
	CreateIssues("Create issues") {

		@Override
		public boolean implies(ProjectPrivilege privilege) {
			return privilege == CreateIssues || ReadIssues.implies(privilege);
		}
		
	},
	EditIssues("Edit issues") {

		@Override
		public boolean implies(ProjectPrivilege privilege) {
			return privilege == EditIssues || CreateIssues.implies(privilege);
		}
		
	},
	DeleteIssues("Delete issues") {

		@Override
		public boolean implies(ProjectPrivilege privilege) {
			return privilege == DeleteIssues || EditIssues.implies(privilege);
		}
		
	},
	ProjectAdministration("Project Administration") {

		@Override
		public boolean implies(ProjectPrivilege privilege) {
			return true;
		}
		
	};

	public abstract boolean implies(ProjectPrivilege privilege);
	
	private final String displayName;
	
	ProjectPrivilege(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
}
