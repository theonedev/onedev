package com.pmease.commons.lang.tokenizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.pmease.commons.util.StringUtils;

public abstract class AbstractTokenizer<S> implements Tokenizer {	
	
	public List<TokenizedLine> tokenize(List<String> lines) {
		List<TokenizedLine> tokenizedLines = new ArrayList<>();
		S state = startState();
		for (String line: lines) {
			TokenizedLine tokenizedLine = new TokenizedLine();
			if (line.length() == 0)
				blankLine(state);
			StringStream stream = new StringStream(line);
			while (!stream.eol()) {
				String style = token(stream, state);
				tokenizedLine.getTokens().add(new Token(style, stream.current()));
				stream.start(stream.pos());
			}
			tokenizedLines.add(tokenizedLine);
		}
		return tokenizedLines;
	}
	
	public List<TokenizedLine> tokenize(String text) {
		return tokenize(Splitter.on("\n").splitToList(text));
	}

	protected abstract S startState();
 	
	protected abstract String token(StringStream stream, S state);
	
	protected void blankLine(S state) {
		
	}
	
	protected int indentUnit() {
		return 2;
	}
	
	protected boolean acceptExtensions(String fileName, String...exts) {
		String thisExt = StringUtils.substringAfterLast(fileName, ".");
		for (String ext: exts) {
			if (ext.equalsIgnoreCase(thisExt))
				return true;
		}
		return false;
	}

	protected boolean acceptPattern(String fileName, Pattern pattern) {
		return pattern.matcher(fileName).matches();
	}
	
	protected static Set<String> wordsOf(String str) {
		Set<String> words = new HashSet<>();
		for (String word: Splitter.on(" ").omitEmptyStrings().trimResults().split(str))
			words.add(word);
		return words;
	}
	
	protected static Set<String> wordsOf(Set<String> words, String str) {
		Set<String> newWords = new HashSet<>(words);
		for (String word: Splitter.on(" ").omitEmptyStrings().trimResults().split(str))
			newWords.add(word);
		return newWords;
	}

}
