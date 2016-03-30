package com.pmease.gitplex.core.security.privilege;

public enum AccountPrivilege implements Privilege {
	
	NONE("None") {

		@Override
		public boolean can(Privilege privilege) {
			return privilege == NONE;
		}
		
	},
	
	// Organization members can access member/team information of organization account 
	ACCESS("Access") {

		@Override
		public boolean can(Privilege privilege) {
			return privilege == ACCESS || NONE.can(privilege);
		}
		
	},
	ADMIN("Admin") {

		@Override
		public boolean can(Privilege privilege) {
			return privilege == ADMIN || ACCESS.can(privilege) || privilege instanceof DepotPrivilege;
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
