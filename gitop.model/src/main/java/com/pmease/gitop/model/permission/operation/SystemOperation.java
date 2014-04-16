package com.pmease.gitop.model.permission.operation;

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
	CREATE_REPOSITORY {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == CREATE_REPOSITORY;
		}
		
	},
	CREATE_PULL_REQUEST {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == CREATE_PULL_REQUEST;
		}
		
	}
}
