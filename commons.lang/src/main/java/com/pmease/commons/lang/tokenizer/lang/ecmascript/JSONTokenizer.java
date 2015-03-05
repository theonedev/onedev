package com.pmease.commons.lang.tokenizer.lang.ecmascript;

public class JSONTokenizer extends AbstractJavaScriptTokenizer {

	public JSONTokenizer() {
		super(false);
	}

	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "json", "map");
	}

	@Override
	protected boolean jsonldMode() {
		return false;
	}

	@Override
	protected boolean json() {
		return true;
	}
	
}