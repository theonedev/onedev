package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

public class MandatoryLiteralScan {
	
	private final List<String> mandatoryLiterals;
	
	private final boolean stop;
	
	/**
	 * Construct mandatory scan object
	 * 
	 * @param mandatoryLiterals
	 * 			list of mandatory literals
	 * @param stop
	 * 			sign to whether or not the mandatory scan should be 
	 * 			stopped
	 */
	public MandatoryLiteralScan(List<String> mandatoryLiterals, boolean stop) {
		this.mandatoryLiterals = mandatoryLiterals;
		this.stop = stop;
	}

	public List<String> getMandatoryLiterals() {
		return mandatoryLiterals;
	}

	public boolean isStop() {
		return stop;
	}

	public static MandatoryLiteralScan stop() {
		return new MandatoryLiteralScan(new ArrayList<String>(), true);
	}
	
}
