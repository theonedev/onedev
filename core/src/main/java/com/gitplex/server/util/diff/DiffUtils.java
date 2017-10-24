package com.gitplex.server.util.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.gitplex.jsyntax.TextToken;
import com.gitplex.jsyntax.TokenTypes;
import com.gitplex.jsyntax.Tokenized;
import com.gitplex.jsyntax.Tokenizer;
import com.gitplex.jsyntax.TokenizerRegistry;
import com.gitplex.server.model.support.TextRange;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.util.diff.DiffMatchPatch.Diff;
import com.gitplex.server.util.diff.DiffMatchPatch.Operation;
import com.gitplex.jsyntax.TokenUtils;
import com.google.common.base.Preconditions;

public class DiffUtils {

	private static final int CHANGE_CALC_TIMEOUT = 100;
	
	public static final int MAX_DIFF_SIZE = 65535;
	
	private static final Pattern pattern = Pattern.compile("\\w+");
	
	private static List<Long> splitByWord(String line, long token) {
		int beginPos = TokenUtils.getBeginPos(token);
		int endPos = TokenUtils.getEndPos(token);
		int typeId = TokenUtils.getTypeId(token);
		String text = TokenUtils.getText(line, token);
		List<Long> tokens = new ArrayList<>();
		Matcher matcher = pattern.matcher(text);
		int lastEnd = 0;
		while (matcher.find()) {
			int start = matcher.start();
			if (start > lastEnd)
				tokens.add(TokenUtils.getToken(lastEnd+beginPos, start+beginPos, typeId));
            tokens.add(TokenUtils.getToken(matcher.start()+beginPos, matcher.end()+beginPos, typeId));
            lastEnd = matcher.end();
        }
		if (lastEnd < text.length())
			tokens.add(TokenUtils.getToken(lastEnd+beginPos, endPos, typeId));
		return tokens;
	}
	
	private static List<Tokenized> tokenize(List<String> lines, @Nullable String fileName) {
		Tokenizer tokenizer = TokenizerRegistry.getTokenizer(fileName);
		if (tokenizer != null) {
			List<Tokenized> tokenizedLines = tokenizer.tokenize(lines);
			List<Tokenized> refinedTokens = new ArrayList<>();
			int index = 0;
			for (Tokenized tokenizedLine: tokenizedLines) {
				List<Long> refinedLine = new ArrayList<>();
				for (long token: tokenizedLine.getTokens()) {
					int typeId = TokenUtils.getTypeId(token);
					if (typeId == 0 || (typeId & TokenTypes.COMMENT) != 0 || (typeId & TokenTypes.STRING) != 0 
							|| (typeId & TokenTypes.STRING2) != 0 || (typeId & TokenTypes.META) != 0
							|| (typeId & TokenTypes.LINK) != 0 || (typeId & TokenTypes.ATTRIBUTE) != 0
							|| (typeId & TokenTypes.PROPERTY) != 0) {
						refinedLine.addAll(splitByWord(lines.get(index), token));
					} else {
						refinedLine.add(token);
					}
				}
				refinedTokens.add(new Tokenized(tokenizedLine.getText(), TokenUtils.toArray(refinedLine)));
				index++;
			}
			return refinedTokens;
		} else {
			List<Tokenized> tokenizedLines = new ArrayList<>();
			for (String line: lines) {
				long token = TokenUtils.getToken(0, line.length(), 0);
				tokenizedLines.add(new Tokenized(line, TokenUtils.toArray(splitByWord(line, token))));
			}
			return tokenizedLines;
		}
	}
	
	/**
	 * Diff two list of strings.
	 */
	public static List<DiffBlock<Tokenized>> diff(List<String> oldLines, @Nullable String oldFileName, 
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
		
		List<Tokenized> oldTokenizedLines = tokenize(oldLines, oldFileName);
		List<Tokenized> newTokenizedLines = tokenize(newLines, newFileName);

		DiffMatchPatch dmp = new DiffMatchPatch();
		TokensToCharsResult<String> result1 = tokensToChars(processedOldLines, processedNewLines);
		
		List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result1.chars1, result1.chars2, false);

		List<DiffBlock<Tokenized>> diffBlocks = new ArrayList<>();
		int oldLineNo = 0;
		int newLineNo = 0;
		for (Diff diff : diffs) {
			List<Tokenized> lines = new ArrayList<>();
			if (diff.operation == Operation.EQUAL) {
				for (int i = 0; i < diff.text.length(); i++) {
					lines.add(newTokenizedLines.get(newLineNo));
					oldLineNo++;
					newLineNo++;
				}
				diffBlocks.add(new DiffBlock<>(diff.operation, lines, 
						oldLineNo-lines.size(), newLineNo-lines.size()));
			} else if (diff.operation == Operation.INSERT) {
				for (int i = 0; i < diff.text.length(); i++)
					lines.add(newTokenizedLines.get(newLineNo++));
				diffBlocks.add(new DiffBlock<>(diff.operation, lines, 
						oldLineNo, newLineNo-lines.size()));
			} else {
				for (int i = 0; i < diff.text.length(); i++)
					lines.add(oldTokenizedLines.get(oldLineNo++));
				diffBlocks.add(new DiffBlock<>(diff.operation, lines, 
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
			List<Tokenized> deleteLines, List<Tokenized> insertLines) {
		LinkedHashMap<Integer, LineDiff> lineDiffs = new LinkedHashMap<>();
		
		DiffMatchPatch dmp = new DiffMatchPatch();
		
		long time = System.currentTimeMillis();
		int nextInsert = 0;
		for (int i=0; i<deleteLines.size(); i++) {
			Tokenized deleteLine = deleteLines.get(i);
			List<TextToken> deleteTokens = TokenUtils.getTextTokens(deleteLine);
			for (int j=nextInsert; j<insertLines.size(); j++) {
				Tokenized insertLine = insertLines.get(j);
				List<TextToken> insertTokens = TokenUtils.getTextTokens(insertLine);
				
				TokensToCharsResult<TextToken> result = DiffUtils.tokensToChars(deleteTokens, insertTokens);						
				List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result.chars1, result.chars2, false);
				int equal = 0;
				int total = 0;
				for (DiffMatchPatch.Diff diff: diffs) {
					for (int k=0; k<diff.text.length(); k++) {
						int pos = diff.text.charAt(k);
						TextToken token = result.tokenArray.get(pos);
						if (StringUtils.isNotBlank(token.getText())) {
							total += token.getText().length();
							if (diff.operation == Operation.EQUAL)
								equal += token.getText().length();
						}
					}
				}
				if (equal*3 >= total) {
					List<DiffBlock<TextToken>> diffBlocks = new ArrayList<>();
					int oldLineNo = 0;
					int newLineNo = 0;
					for (Diff diff : diffs) {
						List<TextToken> tokens = new ArrayList<>();
						if (diff.operation == Operation.EQUAL) {
							for (int k = 0; k < diff.text.length(); k++) {
								tokens.add(insertTokens.get(newLineNo));
								oldLineNo++;
								newLineNo++;
							}
							diffBlocks.add(new DiffBlock<TextToken>(
									diff.operation, tokens, oldLineNo-tokens.size(), newLineNo-tokens.size()));
						} else if (diff.operation == Operation.INSERT) {
							for (int k = 0; k < diff.text.length(); k++)
								tokens.add(insertTokens.get(newLineNo++));
							diffBlocks.add(new DiffBlock<>(
									diff.operation, tokens, oldLineNo, newLineNo-tokens.size()));
						} else {
							for (int k = 0; k < diff.text.length(); k++)
								tokens.add(deleteTokens.get(oldLineNo++));
							diffBlocks.add(new DiffBlock<>(
									diff.operation, tokens, oldLineNo-tokens.size(), newLineNo));
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
	
	public static TextRange mapRange(Map<Integer, Integer> lineMapping, TextRange range) {
		int oldBeginLine = range.getBeginLine();
		int oldEndLine = range.getEndLine();
		Integer newBeginLine = lineMapping.get(oldBeginLine);
		Integer newEndLine = lineMapping.get(oldEndLine);
		if (newBeginLine != null && newEndLine != null && newEndLine >= newBeginLine) {
			TextRange newRange = new TextRange(newBeginLine, range.getBeginChar(), 
					newEndLine, range.getEndChar()); 
			return newRange;
		} else {
			return null;
		}
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
