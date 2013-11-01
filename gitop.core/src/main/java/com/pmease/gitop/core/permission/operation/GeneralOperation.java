package com.pmease.gitop.core.permission.operation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

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
	
	public static GeneralOperation mostPermissive(GeneralOperation... operations) {
		return Collections.max(Arrays.asList(operations), new Comparator<GeneralOperation>() {

			@Override
			public int compare(GeneralOperation operation1, GeneralOperation operation2) {
				if (operation1.can(operation2))
					return 1;
				else if (operation2.can(operation1))
					return -1;
				else
					return 0;
			}
			
		});
	}
}
