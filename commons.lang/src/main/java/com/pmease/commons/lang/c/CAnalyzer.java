package com.pmease.commons.lang.c;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;

import com.pmease.commons.lang.AbstractAnalyzer;
import com.pmease.commons.lang.AnalyzeException;
import com.pmease.commons.lang.AnalyzeResult;
import com.pmease.commons.lang.LangStream;
import com.pmease.commons.lang.LangToken;
import com.pmease.commons.lang.Outline;
import com.pmease.commons.lang.TokenFilter;
import com.pmease.commons.lang.java.JavaLexer;

public class CAnalyzer extends AbstractAnalyzer {

	@Override
	public AnalyzeResult analyze(String text) throws AnalyzeException {
		LangStream stream = new LangStream(
				new CLexer(new ANTLRInputStream(text)), TokenFilter.DEFAULT_CHANNEL);
		List<LangToken> symbols = stream.allType(CLexer.Identifier);
		AnalyzeResult result = new AnalyzeResult(symbols, null);
		return result;
	}

	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "c", "h");
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

}
