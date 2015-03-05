package com.pmease.commons.lang.tokenizer.lang.ecmascript;


public class TypeScriptTokenizer extends AbstractJavaScriptTokenizer {

	public TypeScriptTokenizer() {
		super(true);
	}

	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "ts");
	}

	@Override
	protected boolean jsonldMode() {
		return false;
	}

	@Override
	protected boolean json() {
		return false;
	}
	
}