package com.pmease.commons.lang;

import java.util.List;

import javax.annotation.Nullable;

public class AnalyzeResult {
	
	private final List<LangToken> symbols;
	
	private final Outline outline;
	
	public AnalyzeResult(List<LangToken> symbols, @Nullable Outline outline) {
		this.symbols = symbols;
		this.outline = outline;
	}

	public List<LangToken> getSymbols() {
		return symbols;
	}

	@Nullable
	public Outline getOutline() {
		return outline;
	}
	
}
