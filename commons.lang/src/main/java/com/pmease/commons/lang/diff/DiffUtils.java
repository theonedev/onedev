package com.pmease.commons.lang.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.pmease.commons.lang.diff.DiffMatchPatch.Diff;
import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.tokenizers.CmToken;
import com.pmease.commons.lang.tokenizers.Tokenizers;
import com.pmease.commons.loader.AppLoader;

public class DiffUtils {

	private static final int MAX_DIFF_LEN = 65535;
	
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
	
	private static List<List<CmToken>> tokenize(List<String> lines, @Nullable String fileName) {
		List<List<CmToken>> tokenizedLines = null;
		if (fileName != null)
			tokenizedLines = AppLoader.getInstance(Tokenizers.class).tokenize(lines, fileName);
		if (tokenizedLines != null) {
			List<List<CmToken>> refinedLines = new ArrayList<>();
			for (List<CmToken> tokenizedLine: tokenizedLines) {
				List<CmToken> refinedLine = new ArrayList<>();
				for (CmToken token: tokenizedLine) {
					if (token.getType().equals("") || token.isComment() || token.isString() 
							|| token.isMeta() || token.isLink() || token.isAttribute() 
							|| token.isProperty()) {
						for (String each: splitByWords(token.getText()))
							refinedLine.add(new CmToken(token.getType(), each));
					} else {
						refinedLine.add(token);
					}
				}
				refinedLines.add(refinedLine);
			}
			return refinedLines;
		} else {
			tokenizedLines = new ArrayList<>();
			for (String line: lines) {
				List<CmToken> tokenizedLine = new ArrayList<>();
				for (String each: splitByWords(line)) 
					tokenizedLine.add(new CmToken(each, ""));
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
		Preconditions.checkArgument(oldLines.size() + newLines.size() <= MAX_DIFF_LEN, 
				"Total size of old lines and new lines should be less than " + MAX_DIFF_LEN + ".");
		
		List<List<CmToken>> oldTokenizedLines = tokenize(oldLines, oldFileName);
		List<List<CmToken>> newTokenizedLines = tokenize(newLines, newFileName);

		DiffMatchPatch dmp = new DiffMatchPatch();
		TokensToCharsResult<String> result1 = tokensToChars(oldLines, newLines);
		
		List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result1.chars1, result1.chars2, false);

		List<DiffBlock> diffBlocks = new ArrayList<>();
		int oldLineNo = 0;
		int newLineNo = 0;
		for (Diff diff : diffs) {
			List<List<CmToken>> lines = new ArrayList<>();
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
				int nextInsert = 0;
				for (List<CmToken> deleteLine: prevBlock.getLines()) {
					for (int i=nextInsert; i<block.getLines().size(); i++) {
						List<CmToken> insertLine = block.getLines().get(i);
						TokensToCharsResult<CmToken> result2 = tokensToChars(deleteLine, insertLine);						
						diffs = dmp.diff_main(result2.chars1, result2.chars2, false);
						int equal = 0;
						int total = 0;
						for (Diff diff: diffs) {
							for (int j=0; j<diff.text.length(); j++) {
								int pos = diff.text.charAt(j);
								CmToken token = result2.tokenArray.get(pos);
								if (StringUtils.isNotBlank(token.getText())) {
									total += token.getText().length();
									if (diff.operation == Operation.EQUAL)
										equal += token.getText().length();
								}
							}
						}
						if (equal*3 >= total) {
							oldLineNo = 0;
							newLineNo = 0;
							for (Diff diff: diffs) {
								if (diff.operation == Operation.EQUAL) {
									oldLineNo += diff.text.length();
									newLineNo += diff.text.length();
								} else if (diff.operation == Operation.INSERT) {
									for (int j=0; j<diff.text.length(); j++) {
										insertLine.get(newLineNo).setChanged(true);
										newLineNo++;
									}
								} else {
									for (int j=0; j<diff.text.length(); j++) {
										deleteLine.get(oldLineNo).setChanged(true);
										oldLineNo++;
									}
								}
							}
							nextInsert = i+1;
							break;
						}
					}
				}
			}
			prevBlock = block;
		}
		return diffBlocks;
	}

	private static <T> TokensToCharsResult<T> tokensToChars(List<T> tokens1, List<T> tokens2) {
		List<T> tokenArray = new ArrayList<>();
		Map<T, Integer> tokenHash = new HashMap<>();
		// e.g. linearray[4] == "Hello\n"
		// e.g. linehash.get("Hello\n") == 4

		// "\x00" is a valid character, but various debuggers don't like it.
		// So we'll insert a junk entry to avoid generating a null character.
		tokenArray.add(null);

		String chars1 = tokensToCharsMunge(tokens1, tokenArray, tokenHash);
		String chars2 = tokensToCharsMunge(tokens2, tokenArray, tokenHash);
		return new TokensToCharsResult<T>(chars1, chars2, tokenArray);
	}

	private static <T> String tokensToCharsMunge(List<T> tokens, List<T> tokenArray, Map<T, Integer> tokenHash) {
		StringBuilder chars = new StringBuilder();
		for (T token: tokens) {
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
	
	private static class TokensToCharsResult<T> {
		private String chars1;
		private String chars2;
		private List<T> tokenArray;

		private TokensToCharsResult(String chars1, String chars2, List<T> tokenArray) {
			this.chars1 = chars1;
			this.chars2 = chars2;
			this.tokenArray = tokenArray;
		}
	}

}
