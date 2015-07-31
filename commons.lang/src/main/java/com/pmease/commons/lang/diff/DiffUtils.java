package com.pmease.commons.lang.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.pmease.commons.lang.diff.DiffMatchPatch.Diff;
import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.tokenizers.Token;
import com.pmease.commons.lang.tokenizers.TokenizedLine;
import com.pmease.commons.lang.tokenizers.Tokenizers;
import com.pmease.commons.loader.AppLoader;

public class DiffUtils {

	private static final Pattern pattern = Pattern.compile("\\w+");
	
	private static List<String> splitByWords(String line) {
		List<String> tokens = new ArrayList<>();
		Matcher matcher = pattern.matcher(line);
		int lastEnd = 0;
		while (matcher.find()) {
			int start = matcher.start();
			if (start > lastEnd)
				tokens.add(line.substring(lastEnd, start));
            tokens.add(matcher.group());
            lastEnd = matcher.end();
        }
		if (lastEnd < line.length())
			tokens.add(line.substring(lastEnd));
		return tokens;
	}
	
	private static List<TokenizedLine> tokenize(List<String> lines, @Nullable String fileName) {
		List<TokenizedLine> tokenizedLines = null;
		if (fileName != null)
			tokenizedLines = AppLoader.getInstance(Tokenizers.class).tokenize(lines, fileName);
		if (tokenizedLines != null) {
			List<TokenizedLine> refinedLines = new ArrayList<>();
			for (TokenizedLine tokenizedLine: tokenizedLines) {
				TokenizedLine refinedLine = new TokenizedLine();
				for (Token token: tokenizedLine.getTokens()) {
					if (token.style().equals("") || token.isComment() || token.isString() 
							|| token.isMeta() || token.isLink() || token.isAttribute() 
							|| token.isProperty()) {
						for (String each: splitByWords(token.text()))
							refinedLine.getTokens().add(new Token(token.style(), each));
					}
				}
				refinedLines.add(refinedLine);
			}
			return refinedLines;
		} else {
			tokenizedLines = new ArrayList<>();
			for (String line: lines) {
				TokenizedLine tokenizedLine = new TokenizedLine();
				for (String each: splitByWords(line)) 
					tokenizedLine.getTokens().add(new Token(each, ""));
				tokenizedLines.add(tokenizedLine);
			}
			return tokenizedLines;
		}
	}
	
	/**
	 * Diff two list of strings.
	 */
	public static List<DiffLine> diff(List<String> oldLines, @Nullable String oldFileName, 
			List<String> newLines, @Nullable String newFileName) {
		Preconditions.checkArgument(oldLines.size() + newLines.size() <= 65535, 
				"Total size of old lines and new lines should be less than 65535.");
		
		List<TokenizedLine> oldTokenizedLines = tokenize(oldLines, oldFileName);
		List<TokenizedLine> newTokenizedLines = tokenize(newLines, newFileName);

		DiffMatchPatch dmp = new DiffMatchPatch();
		TokensToCharsResult result = tokensToChars(oldLines, newLines);
		
		List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result.chars1, result.chars2, false);

		List<DiffLine> diffLines = new ArrayList<>();
		int oldLineNo = 0;
		int newLineNo = 0;
		Diff prevDiff = null;
		for (Diff diff : diffs) {
			
			if (diff.operation == Operation.EQUAL) {
				for (int i = 0; i < diff.text.length(); i++) {
					List<Token> tokens;
					tokens = newTokenizedLines.get(newLineNo).getTokens();
					diffLines.add(new DiffLine(diff.operation, tokens, oldLineNo++, newLineNo++));
				}
			}
			prevDiff = diff;
		}
		return diffLines;
	}

	private static TokensToCharsResult tokensToChars(List<String> tokens1, List<String> tokens2) {
		List<String> tokenArray = new ArrayList<>();
		Map<String, Integer> tokenHash = new HashMap<>();
		// e.g. linearray[4] == "Hello\n"
		// e.g. linehash.get("Hello\n") == 4

		// "\x00" is a valid character, but various debuggers don't like it.
		// So we'll insert a junk entry to avoid generating a null character.
		tokenArray.add("");

		String chars1 = tokensToCharsMunge(tokens1, tokenArray, tokenHash);
		String chars2 = tokensToCharsMunge(tokens2, tokenArray, tokenHash);
		return new TokensToCharsResult(chars1, chars2);
	}

	private static String tokensToCharsMunge(List<String> tokens, List<String> tokenArray, Map<String, Integer> tokenHash) {
		StringBuilder chars = new StringBuilder();
		for (String token: tokens) {
			if (tokenHash.containsKey(token)) {
				chars.append(String.valueOf((char) (int) tokenHash.get(token)));
			} else {
				tokenArray.add(token);
				tokenHash.put(token, tokenArray.size() - 1);
				chars.append(String.valueOf((char) (tokenArray.size() - 1)));
			}
		}
		return chars.toString();
	}
	
	private static class TokensToCharsResult {
		private String chars1;
		private String chars2;

		private TokensToCharsResult(String chars1, String chars2) {
			this.chars1 = chars1;
			this.chars2 = chars2;
		}
	}

}
