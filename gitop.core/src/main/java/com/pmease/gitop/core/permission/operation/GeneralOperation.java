package com.pmease.gitop.core.permission.operation;

public enum GeneralOperation implements PrivilegedOperation {
	
	NO_ACCESS("No Access") {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return false;
		}
		
	},
	READ("Read") {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == READ;
		}
		
	},
	WRITE("Write") {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == WRITE || READ.can(operation);
		}
		
	},
	ADMIN("Admin") {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return true;
		}
		
	};

	private final String displayName;
	
	GeneralOperation(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
	
}
