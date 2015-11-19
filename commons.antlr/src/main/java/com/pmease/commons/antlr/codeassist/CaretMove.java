package com.pmease.commons.antlr.codeassist;

public class CaretMove {
	
	private final int offset;
	
	private final boolean stop;
	
	public CaretMove(int offset, boolean stop) {
		this.offset = offset;
		this.stop = stop;
	}

	public int getOffset() {
		return offset;
	}

	public boolean isStop() {
		return stop;
	}
	
}
