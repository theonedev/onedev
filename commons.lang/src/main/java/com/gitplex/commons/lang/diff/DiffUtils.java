package com.gitplex.commons.lang.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.gitplex.commons.lang.diff.DiffMatchPatch.Diff;
import com.gitplex.commons.lang.diff.DiffMatchPatch.Operation;
import com.gitplex.commons.util.StringUtils;
import com.gitplex.jsyntax.Token;
import com.gitplex.jsyntax.Tokenizers;
import com.google.common.base.Preconditions;

public class DiffUtils {

	private static final int CHANGE_CALC_TIMEOUT = 100;
	
	public static final int MAX_DIFF_SIZE = 65535;
	
	private static final Pattern pattern = Pattern.compile("\\w+");
	
	private static List<String> splitByWord(String line) {
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
			tokenizedLines = Tokenizers.tokenize(lines, fileName);
		if (tokenizedLines != null) {
			List<List<Token>> refinedLines = new ArrayList<>();
			for (List<Token> tokenizedLine: tokenizedLines) {
				List<Token> refinedLine = new ArrayList<>();
				for (Token token: tokenizedLine) {
					if (token.getType().equals("") || token.isComment() || token.isString() 
							|| token.isMeta() || token.isLink() || token.isAttribute() 
							|| token.isProperty()) {
						for (String each: splitByWord(token.getText()))
							refinedLine.add(new Token(token.getType(), each));
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
				List<Token> tokenizedLine = new ArrayList<>();
				for (String each: splitByWord(line)) 
					tokenizedLine.add(new Token("", each));
				tokenizedLines.add(tokenizedLine);
			}
			return tokenizedLines;
		}
	}
	
	/**
	 * Diff two list of strings.
	 */
	public static List<DiffBlock<List<Token>>> diff(List<String> oldLines, @Nullable String oldFileName, 
			List<String> newLines, @Nullable String newFileName, WhitespaceOption whitespaceOption) {
		Preconditions.checkArgument(oldLines.size() + newLines.size() <= MAX_DIFF_SIZE, 
				"Total size of old lines and new lines should be less than " + MAX_DIFF_SIZE + ".");
		
		List<String> processedOldLines = new ArrayList<>();
		for (String line: oldLines) {
			processedOldLines.add(whitespaceOption.process(line));
		}
		
		List<String> processedNewLines = new ArrayList<>();
		for (String line: newLines) {
			processedNewLines.add(whitespaceOption.process(line));
		}
		
		List<List<Token>> oldTokenizedLines = tokenize(oldLines, oldFileName);
		List<List<Token>> newTokenizedLines = tokenize(newLines, newFileName);

		DiffMatchPatch dmp = new DiffMatchPatch();
		TokensToCharsResult<String> result1 = tokensToChars(processedOldLines, processedNewLines);
		
		List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result1.chars1, result1.chars2, false);

		List<DiffBlock<List<Token>>> diffBlocks = new ArrayList<>();
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
				diffBlocks.add(new DiffBlock<List<Token>>(diff.operation, lines, 
						oldLineNo-lines.size(), newLineNo-lines.size()));
			} else if (diff.operation == Operation.INSERT) {
				for (int i = 0; i < diff.text.length(); i++)
					lines.add(newTokenizedLines.get(newLineNo++));
				diffBlocks.add(new DiffBlock<List<Token>>(diff.operation, lines, 
						oldLineNo, newLineNo-lines.size()));
			} else {
				for (int i = 0; i < diff.text.length(); i++)
					lines.add(oldTokenizedLines.get(oldLineNo++));
				diffBlocks.add(new DiffBlock<List<Token>>(diff.operation, lines, 
						oldLineNo-lines.size(), newLineNo));
			}
		}
		
		return diffBlocks;
	}
	
	public static <T> List<DiffBlock<T>> diff(List<T> oldLines, List<T> newLines) {
		Preconditions.checkArgument(oldLines.size() + newLines.size() <= MAX_DIFF_SIZE, 
				"Total size of old lines and new lines should be less than " + MAX_DIFF_SIZE + ".");
		
		DiffMatchPatch dmp = new DiffMatchPatch();
		TokensToCharsResult<T> result1 = tokensToChars(oldLines, newLines);
		
		List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result1.chars1, result1.chars2, false);

		List<DiffBlock<T>> diffBlocks = new ArrayList<>();
		int oldLineNo = 0;
		int newLineNo = 0;
		for (Diff diff : diffs) {
			List<T> lines = new ArrayList<>();
			if (diff.operation == Operation.EQUAL) {
				for (int i = 0; i < diff.text.length(); i++) {
					lines.add(newLines.get(newLineNo));
					oldLineNo++;
					newLineNo++;
				}
				diffBlocks.add(new DiffBlock<T>(diff.operation, lines, oldLineNo-lines.size(), newLineNo-lines.size()));
			} else if (diff.operation == Operation.INSERT) {
				for (int i = 0; i < diff.text.length(); i++)
					lines.add(newLines.get(newLineNo++));
				diffBlocks.add(new DiffBlock<T>(diff.operation, lines, oldLineNo, newLineNo-lines.size()));
			} else {
				for (int i = 0; i < diff.text.length(); i++)
					lines.add(oldLines.get(oldLineNo++));
				diffBlocks.add(new DiffBlock<T>(diff.operation, lines, oldLineNo-lines.size(), newLineNo));
			}
		}
		
		return diffBlocks;
	}
	
	/**
	 * This method checks deleted lines and inserted lines, and position them so that 
	 * similar delete line and insert line (indicates they are the same line with 
	 * modification) will be displayed on same row
	 * @param deleteLines
	 * @param insertLines
	 * @return
	 */
	public static LinkedHashMap<Integer, LineDiff> align(
			List<List<Token>> deleteLines, List<List<Token>> insertLines) {
		LinkedHashMap<Integer, LineDiff> lineDiffs = new LinkedHashMap<>();
		
		DiffMatchPatch dmp = new DiffMatchPatch();
		
		long time = System.currentTimeMillis();
		int nextInsert = 0;
		for (int i=0; i<deleteLines.size(); i++) {
			List<Token> deleteLine = deleteLines.get(i);
			for (int j=nextInsert; j<insertLines.size(); j++) {
				List<Token> insertLine = insertLines.get(j);
				TokensToCharsResult<Token> result = DiffUtils.tokensToChars(deleteLine, insertLine);						
				List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result.chars1, result.chars2, false);
				int equal = 0;
				int total = 0;
				for (DiffMatchPatch.Diff diff: diffs) {
					for (int k=0; k<diff.text.length(); k++) {
						int pos = diff.text.charAt(k);
						Token token = result.tokenArray.get(pos);
						if (StringUtils.isNotBlank(token.getText())) {
							total += token.getText().length();
							if (diff.operation == Operation.EQUAL)
								equal += token.getText().length();
						}
					}
				}
				if (equal*3 >= total) {
					List<DiffBlock<Token>> diffBlocks = new ArrayList<>();
					int oldLineNo = 0;
					int newLineNo = 0;
					for (Diff diff : diffs) {
						List<Token> tokens = new ArrayList<>();
						if (diff.operation == Operation.EQUAL) {
							for (int k = 0; k < diff.text.length(); k++) {
								tokens.add(insertLine.get(newLineNo));
								oldLineNo++;
								newLineNo++;
							}
							diffBlocks.add(new DiffBlock<Token>(diff.operation, tokens, oldLineNo-tokens.size(), newLineNo-tokens.size()));
						} else if (diff.operation == Operation.INSERT) {
							for (int k = 0; k < diff.text.length(); k++)
								tokens.add(insertLine.get(newLineNo++));
							diffBlocks.add(new DiffBlock<Token>(diff.operation, tokens, oldLineNo, newLineNo-tokens.size()));
						} else {
							for (int k = 0; k < diff.text.length(); k++)
								tokens.add(deleteLine.get(oldLineNo++));
							diffBlocks.add(new DiffBlock<Token>(diff.operation, tokens, oldLineNo-tokens.size(), newLineNo));
						}
					}

					LineDiff lineDiff = new LineDiff(j, diffBlocks);
					lineDiffs.put(i, lineDiff);
					nextInsert = j+1;
					break;
				} else {
					if (System.currentTimeMillis()-time > CHANGE_CALC_TIMEOUT) {
						nextInsert = insertLines.size();
						break;
					}
				}
			}
		}
		return lineDiffs;
	}

	public static <T> Map<Integer, Integer> mapLines(List<T> oldLines, List<T> newLines) {
		return mapLines(diff(oldLines, newLines));
	}
	
	public static <T> int getNewLineAround(List<T> oldLines, List<T> newLines, int oldLine) {
		Map<Integer, Integer> lineMap = mapLines(oldLines, newLines);
		for (int line=oldLine; line>=0; line--) {
			Integer newLine = lineMap.get(line);
			if (newLine != null)
				return newLine;
		}
		return 0;
	}	
	
	public static <T> Map<Integer, Integer> mapLines(List<DiffBlock<T>> diffBlocks) {
		Map<Integer, Integer> lineMapping = new HashMap<Integer, Integer>();
		for (DiffBlock<T> diffBlock: diffBlocks) {
			if (diffBlock.getOperation() == Operation.EQUAL) {
				for (int i=0; i<diffBlock.getUnits().size(); i++)
					lineMapping.put(i+diffBlock.getOldStart(), i+diffBlock.getNewStart());
			}
		}
		return lineMapping;
	}
	
	public static <T> TokensToCharsResult<T> tokensToChars(List<T> tokens1, List<T> tokens2) {
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
