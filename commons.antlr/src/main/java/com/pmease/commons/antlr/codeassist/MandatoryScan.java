package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

public class MandatoryScan {
	
	private final List<String> mandatories;
	
	private final boolean stop;
	
	/**
	 * Construct mandatory scan object
	 * 
	 * @param mandatories
	 * 			list of mandatory literals
	 * @param stop
	 * 			sign to whether or not the mandatory scan should be 
	 * 			stopped
	 */
	public MandatoryScan(List<String> mandatories, boolean stop) {
		this.mandatories = mandatories;
		this.stop = stop;
	}

	public List<String> getMandatories() {
		return mandatories;
	}

	public boolean isStop() {
		return stop;
	}

	public static MandatoryScan stop() {
		return new MandatoryScan(new ArrayList<String>(), true);
	}
	
}
