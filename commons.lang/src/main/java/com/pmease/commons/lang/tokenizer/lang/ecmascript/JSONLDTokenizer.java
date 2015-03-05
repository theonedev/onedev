package com.pmease.commons.lang.tokenizer.lang.ecmascript;

public class JSONLDTokenizer extends AbstractJavaScriptTokenizer {

	public JSONLDTokenizer() {
		super(false);
	}

	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "jsonld");
	}

	@Override
	protected boolean jsonldMode() {
		return true;
	}

	@Override
	protected boolean json() {
		return false;
	}
	
}