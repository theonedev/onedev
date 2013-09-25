package com.pmease.commons.util.execution;

import com.pmease.commons.util.GeneralException;

public class ExecuteResult {

	private int returnCode;
	
	private String errorMessage;
	
	private String commandDescription;

	public ExecuteResult(Commandline cmdline) {
		commandDescription = cmdline.toString();
	}
	
	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	/**
	 * Build an exception object with command description, stderr output and return code.
	 * @return
	 */
	public RuntimeException buildException() {
    	if (errorMessage != null) {
            throw new GeneralException("Failed to run command: " + commandDescription  + 
            		"\nCommand return code: " + getReturnCode() + 
                    "\nCommand error output: " + errorMessage);
    	} else {
            throw new GeneralException("Failed to run command: " + commandDescription + 
            		"\nCommand return code: " + getReturnCode());
    	}
		
	}
	
	/**
	 * Check return code and throw exception if it does not equal to 0.
	 */
	public void checkReturnCode() {
		if (getReturnCode() != 0)
			throw buildException();
	}
}