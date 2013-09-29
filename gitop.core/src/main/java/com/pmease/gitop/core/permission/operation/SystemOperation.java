package com.pmease.gitop.core.permission.operation;

public enum SystemOperation implements PrivilegedOperation {
	ADMINISTRATION {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return true;
		}
		
	},
	ADD_COMMENT {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == ADD_COMMENT;
		}
		
	},
	CREATE_PROJECT {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == CREATE_PROJECT;
		}
		
	},
	CREATE_MERGE_REQUEST {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == CREATE_MERGE_REQUEST;
		}
		
	}
}
