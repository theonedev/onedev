package com.pmease.commons.lang.tokenizer.lang.ecmascript;

public class JavaScriptTokenizer extends AbstractJavaScriptTokenizer {

	public JavaScriptTokenizer() {
		super(false);
	}

	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "js");
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