package com.gitplex.server.security;

public enum ProjectPrivilege {

	READ("Read") {

		@Override
		public boolean implies(ProjectPrivilege privilege) {
			return privilege == READ;
		}
		
	},
	WRITE("Write") {

		@Override
		public boolean implies(ProjectPrivilege privilege) {
			return privilege == WRITE || READ.implies(privilege);
		}
		
	},
	ADMIN("Admin") {

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
