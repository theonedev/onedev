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
	
	private static List<List<Token>> tokenize(List<String> lines, @Nullable String fileName) {
		List<List<Token>> tokenizedLines = null;
		if (fileName != null)
			tokenizedLines = AppLoader.getInstance(Tokenizers.class).tokenize(lines, fileName);
		if (tokenizedLines != null) {
			List<List<Token>> refinedLines = new ArrayList<>();
			for (List<Token> tokenizedLine: tokenizedLines) {
				List<Token> refinedLine = new ArrayList<>();
				for (Token token: tokenizedLine) {
					if (token.style().equals("") || token.isComment() || token.isString() 
							|| token.isMeta() || token.isLink() || token.isAttribute() 
							|| token.isProperty()) {
						for (String each: splitByWords(token.text()))
							refinedLine.add(new Token(token.style(), each));
					}
				}
				refinedLines.add(refinedLine);
			}
			return refinedLines;
		} else {
			tokenizedLines = new ArrayList<>();
			for (String line: lines) {
				List<Token> tokenizedLine = new ArrayList<>();
				for (String each: splitByWords(line)) 
					tokenizedLine.add(new Token(each, ""));
				tokenizedLines.add(tokenizedLine);
			}
			return tokenizedLines;
		}
	}
	
	/**
	 * Diff two list of strings.
	 */
	public static List<DiffBlock> diff(List<String> oldLines, @Nullable String oldFileName, 
			List<String> newLines, @Nullable String newFileName) {
		Preconditions.checkArgument(oldLines.size() + newLines.size() <= 65535, 
				"Total size of old lines and new lines should be less than 65535.");
		
		List<List<Token>> oldTokenizedLines = tokenize(oldLines, oldFileName);
		List<List<Token>> newTokenizedLines = tokenize(newLines, newFileName);

		DiffMatchPatch dmp = new DiffMatchPatch();
		TokensToCharsResult result = tokensToChars(oldLines, newLines);
		
		List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result.chars1, result.chars2, false);

		List<DiffBlock> diffBlocks = new ArrayList<>();
		int oldLineNo = 0;
		int newLineNo = 0;
		for (Diff diff : diffs) {
			List<List<Token>> lines = new ArrayList<>();
			if (diff.operation == Operation.EQUAL) {
				for (int i = 0; i < diff.text.length(); i++) {
					lines.add(newTokenizedLines.get(newLineNo));
					oldLineNo++;
					newLineNo++;
				}
			} else if (diff.operation == Operation.INSERT) {
				for (int i = 0; i < diff.text.length(); i++)
					lines.add(newTokenizedLines.get(newLineNo++));
			} else {
				for (int i = 0; i < diff.text.length(); i++)
					lines.add(oldTokenizedLines.get(oldLineNo++));
			}
			diffBlocks.add(new DiffBlock(diff.operation, lines, oldLineNo, newLineNo));
		}
		
		DiffBlock prevBlock =  null;
		for (DiffBlock block: diffBlocks) {
			if (block.getOperation() == Operation.INSERT && prevBlock != null 
					&& prevBlock.getOperation() == Operation.DELETE) {
				for (List<Token> prevLine: prevBlock.getLines()) {
					for (List<Token> line: block.getLines()) {
					}
				}
			}
			prevBlock = block;
		}
		return diffBlocks;
	}

	private static TokensToCharsResult tokensToChars(List<String> tokens1, List<String> tokens2) {
		Map<String, Integer> tokenHash = new HashMap<>();
		// e.g. linearray[4] == "Hello\n"
		// e.g. linehash.get("Hello\n") == 4

		String chars1 = tokensToCharsMunge(tokens1, tokenHash);
		String chars2 = tokensToCharsMunge(tokens2, tokenHash);
		return new TokensToCharsResult(chars1, chars2);
	}

	private static String tokensToCharsMunge(List<String> tokens, Map<String, Integer> tokenHash) {
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
