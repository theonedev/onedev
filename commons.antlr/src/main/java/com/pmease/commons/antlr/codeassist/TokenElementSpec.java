package com.pmease.commons.antlr.codeassist;

public abstract class TokenElementSpec extends ElementSpec {

	protected final int type;

	public TokenElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity, int type) {
		super(codeAssist, label, multiplicity);
		
		this.type = type;
	}

	public int getType() {
		return type;
	}

}
