package com.pmease.gitop.core.permission.operation;

public enum GeneralOperation implements PrivilegedOperation {
	
	NO_ACCESS("No Access", 100) {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return false;
		}
		
	},
	READ("Read", 1000) {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == READ;
		}
		
	},
	WRITE("Write", 3000) {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == WRITE || READ.can(operation);
		}
		
	},
	ADMIN("Admin", 5000) {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return true;
		}
		
	};

	private final String displayName;
	private final int weight;
	
	GeneralOperation(String displayName, int weight) {
		this.displayName = displayName;
		this.weight = weight;
	}

	@Override
	public String toString() {
		return displayName;
	}
	
	public static GeneralOperation higher(GeneralOperation op1, GeneralOperation op2) {
		if (op1 == null) {
			return op2;
		}
		
		if (op2 == null) {
			return op1;
		}
		
		return op1.weight > op2.weight ? op1 : op2;
	}
}
