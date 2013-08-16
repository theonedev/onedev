package com.pmease.gitop.core.model.permission.operation;

public enum SystemOperation implements PrivilegedOperation {
	ADMINISTRATION {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return true;
		}
		
	},
	CREATE_ASSESSMENT {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == CREATE_ASSESSMENT;
		}
		
	},
	CREATE_COMMENT {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return operation == CREATE_COMMENT;
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
			return operation == READ_ALL_REPOSITORIES;
		}
		
	},
	WRITE_ALL_REPOSITORIES {

		@Override
		public boolean can(PrivilegedOperation operation) {
			return READ_ALL_REPOSITORIES.can(operation);
		}
		
	}
}
