package com.pmease.gitop.core.permission.operation;

public enum SystemOperation implements PrivilegedOperation {
	ADMINISTRATION {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return true;
		}
		
	},
	VOTE {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == VOTE;
		}
		
	},
	ADD_COMMENT {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == ADD_COMMENT;
		}
		
	},
	CREATE_MERGE_REQUEST {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == CREATE_MERGE_REQUEST;
		}
		
	},
	CREATE_REPOSITORY {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == CREATE_REPOSITORY;
		}
		
	},
	READ_ALL_REPOSITORIES {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == READ_ALL_REPOSITORIES || RepositoryOperation.READ.can(operation);
		}
		
	},
	WRITE_ALL_REPOSITORIES {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == WRITE_ALL_REPOSITORIES 
					|| READ_ALL_REPOSITORIES.can(operation) 
					|| RepositoryOperation.WRITE.can(operation);
		}
		
	}
}
