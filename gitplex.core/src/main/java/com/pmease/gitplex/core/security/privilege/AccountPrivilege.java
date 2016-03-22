package com.pmease.gitplex.core.security.privilege;

public enum AccountPrivilege implements Privilege {
	
	NONE("None") {

		@Override
		public boolean can(Privilege privilege) {
			return privilege == NONE;
		}
		
	},
	MEMBER("Member") {

		@Override
		public boolean can(Privilege privilege) {
			return privilege == MEMBER || NONE.can(privilege);
		}
		
	},
	ADMIN("Admin") {

		@Override
		public boolean can(Privilege privilege) {
			return privilege == ADMIN || MEMBER.can(privilege) || privilege instanceof DepotPrivilege;
		}
		
	};

	private final String displayName;
	
	AccountPrivilege(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
}
