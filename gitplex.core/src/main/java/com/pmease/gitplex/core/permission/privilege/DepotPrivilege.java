package com.pmease.gitplex.core.permission.privilege;

public enum DepotPrivilege implements Privilege {
	
	NONE("None") {

		@Override
		public boolean can(Privilege privilege) {
			return privilege == NONE;
		}
		
	},
	PULL("Pull") {

		@Override
		public boolean can(Privilege privilege) {
			return privilege == PULL || NONE.can(privilege);
		}
		
	},
	PUSH("Push") {

		@Override
		public boolean can(Privilege privilege) {
			return privilege == PUSH || PULL.can(privilege);
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
