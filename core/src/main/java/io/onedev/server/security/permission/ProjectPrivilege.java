package io.onedev.server.security.permission;

public enum ProjectPrivilege {
	ISSUE_READ("Issue read") {

		@Override
		public boolean implies(ProjectPrivilege privilege) {
			return privilege == ISSUE_READ;
		}
		
	},
	CODE_READ("Code read") {

		@Override
		public boolean implies(ProjectPrivilege privilege) {
			return privilege == CODE_READ || ISSUE_READ.implies(privilege);
		}
		
	},
	CODE_WRITE("Code write") {

		@Override
		public boolean implies(ProjectPrivilege privilege) {
			return privilege == CODE_WRITE || CODE_READ.implies(privilege);
		}
		
	},
	PROJECT_ADMINISTRATION("Project administration") {

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
