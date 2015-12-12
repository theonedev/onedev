package com.pmease.commons.antlr.codeassist;

import java.util.Set;

public class AnyTokenElementSpec extends TerminalElementSpec {

	private static final long serialVersionUID = 1L;

	public AnyTokenElementSpec(String label, Multiplicity multiplicity) {
		super(label, multiplicity);
	}

	@Override
	public MandatoryScan scanMandatories(Set<String> checkedRules) {
		return MandatoryScan.stop();
	}

	@Override
	protected String toStringOnce() {
		return ".";
	}

	@Override
	public boolean isToken(int tokenType) {
		return true;
	}

}
