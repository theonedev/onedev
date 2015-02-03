package com.pmease.commons.wicket.behavior.testform;

public interface TestResult {
	String getMessage();
	
	boolean isSuccessful();
	
	public static class Successful implements TestResult {

		private final String message;
		
		public Successful(String message) {
			this.message = message;
		}
		
		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public boolean isSuccessful() {
			return true;
		}
		
	}
	
	public static class Failed implements TestResult {

		private final String message;
		
		public Failed(String message) {
			this.message = message;
		}
		
		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public boolean isSuccessful() {
			return false;
		}
		
	}
	
}
