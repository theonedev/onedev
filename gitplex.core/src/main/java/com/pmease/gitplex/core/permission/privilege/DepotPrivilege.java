package com.pmease.gitplex.core.permission.privilege;

public enum DepotPrivilege implements Privilege {
	
	NONE("None") {

		@Override
		public boolean can(Privilege privilege) {
			return privilege == NONE;
		}
		
	},
	READ("Read") {

		@Override
		public boolean can(Privilege privilege) {
			return privilege == READ || NONE.can(privilege);
		}
		
	},
	WRITE("Write") {

		@Override
		public boolean can(Privilege privilege) {
			return privilege == WRITE || READ.can(privilege);
		}
		
	},
	ADMIN("Admin") {

		@Override
		public boolean can(Privilege privilege) {
			return true;
		}
		
	};

	private final String displayName;
	
	DepotPrivilege(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
}
