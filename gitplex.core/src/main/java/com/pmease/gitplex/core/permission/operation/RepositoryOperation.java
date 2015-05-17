package com.pmease.gitplex.core.permission.operation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public enum RepositoryOperation implements PrivilegedOperation {
	
	NO_ACCESS("No Access") {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == NO_ACCESS;
		}
		
	},
	PULL("Pull") {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == PULL || NO_ACCESS.can(operation);
		}
		
	},
	PUSH("Push") {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == PUSH || PULL.can(operation);
		}
		
	},
	ADMIN("Admin") {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return true;
		}
		
	};

	private final String displayName;
	
	RepositoryOperation(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
	public static RepositoryOperation mostPermissive(RepositoryOperation... operations) {
		return Collections.max(Arrays.asList(operations), new Comparator<RepositoryOperation>() {

			@Override
			public int compare(RepositoryOperation operation1, RepositoryOperation operation2) {
				if (operation1 == operation2)
					return 0;
				else if (operation1.can(operation2))
					return 1;
				else if (operation2.can(operation1))
					return -1;
				else
					return 0;
			}
			
		});
	}
}
