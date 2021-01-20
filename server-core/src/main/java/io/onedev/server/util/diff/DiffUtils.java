package io.onedev.server.util.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import io.onedev.commons.jsyntax.TextToken;
import io.onedev.commons.jsyntax.TokenTypes;
import io.onedev.commons.jsyntax.TokenUtils;
import io.onedev.commons.jsyntax.Tokenized;
import io.onedev.commons.jsyntax.Tokenizer;
import io.onedev.commons.jsyntax.TokenizerRegistry;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.util.diff.DiffMatchPatch.Diff;
import io.onedev.server.util.diff.DiffMatchPatch.Operation;

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
	
	public static List<String> getLines(@Nullable String text) {
		List<String> lines = new ArrayList<>();
		if (text != null)
			lines = Splitter.on("\n").splitToList(text);
		else
			lines = new ArrayList<>();
		return lines;
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
				/* 
				 * Do not tokenize line if file name is not specified, as sometimes we want to 
				 * show addition/deletion as a whole. To tokenize line as plain text, specify 
				 * file name with a .txt suffix
				 */
				if (line.length() != 0) {
					long[] tokens = new long[1];
					tokens[0] = TokenUtils.getToken(0, line.length(), 0); 
					tokenizedLines.add(new Tokenized(line, tokens));
				} else {
					tokenizedLines.add(new Tokenized(line, new long[0]));
				}
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
		for (String line: oldLines) 
			processedOldLines.add(whitespaceOption.process(line));
		
		List<String> processedNewLines = new ArrayList<>();
		for (String line: newLines) 
			processedNewLines.add(whitespaceOption.process(line));
		
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
			List<Tokenized> deleteLines, List<Tokenized> insertLines, boolean forceAlign) {
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
				if (forceAlign || equal*3 >= total) {
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
	
	public static <T> boolean isVisible(List<DiffBlock<T>> diffBlocks, boolean leftSide, 
			int line, int contextSize) {
		if (leftSide) {
			for (int i=0; i<diffBlocks.size(); i++) {
				DiffBlock<T> block = diffBlocks.get(i);
				if (block.getOperation() == Operation.INSERT)
					continue;
				if (line>=block.getOldStart() && line<block.getOldEnd()) {
					if (block.getOperation() == Operation.DELETE) 
						return true;
					else if (i == 0 && i == diffBlocks.size()-1) 
						return false;
					else if (i != 0 && i != diffBlocks.size()-1)
						return line < block.getOldStart() + contextSize || line >= block.getOldEnd()-contextSize;
					else if (i == 0) 
						return line >= block.getOldEnd()-contextSize;
					else 
						return line < block.getOldStart() + contextSize;
				} 
			}
		} else {
			for (int i=0; i<diffBlocks.size(); i++) {
				DiffBlock<T> block = diffBlocks.get(i);
				if (block.getOperation() == Operation.DELETE)
					continue;
				if (line>=block.getNewStart() && line<block.getNewEnd()) {
					if (block.getOperation() == Operation.INSERT) 
						return true;
					else if (i == 0 && i == diffBlocks.size()-1) 
						return false;
					else if (i != 0 && i != diffBlocks.size()-1)
						return line < block.getNewStart() + contextSize || line >= block.getNewEnd()-contextSize;
					else if (i == 0) 
						return line >= block.getNewEnd()-contextSize;
					else 
						return line < block.getNewStart() + contextSize;
				} 
			}
		}
		return false;
	}
	
	public static PlanarRange mapRange(Map<Integer, Integer> lineMapping, PlanarRange range) {
		int oldBeginLine = range.getFromRow();
		int oldEndLine = range.getToRow();
		Integer newBeginLine = lineMapping.get(oldBeginLine);
		if (newBeginLine != null) {
			for (int oldLine = oldBeginLine+1; oldLine < oldEndLine; oldLine++) {
				Integer newLine = lineMapping.get(oldLine);
				if (newLine == null || newLine - newBeginLine != oldLine - oldBeginLine)
					return null;
			}
			PlanarRange newRange = new PlanarRange(newBeginLine, range.getFromColumn(), 
					newBeginLine + oldEndLine - oldBeginLine, range.getToColumn()); 
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

	private static void appendTokenized(StringBuilder builder, Tokenized tokenized) {
		if (tokenized.getTokens().length == 0) {
			builder.append("&nbsp;");
		} else {
			for (long token: tokenized.getTokens()) {
				builder.append(TokenUtils.toHtml(tokenized.getText(), token, null, null));
			}
		}
	}
	
	private static void appendDeletesAndInserts(StringBuilder builder, DiffBlock<Tokenized> deleteBlock, 
			DiffBlock<Tokenized> insertBlock, int fromDeleteLineIndex, int toDeleteLineIndex, 
		int fromInsertLineIndex, int toInsertLineIndex) {
		for (int i=fromDeleteLineIndex; i<toDeleteLineIndex; i++)
			appendDelete(builder, deleteBlock, i, null);
		for (int i=fromInsertLineIndex; i<toInsertLineIndex; i++)
			appendInsert(builder, insertBlock, i, null);
	}
	
	private static void appendInsert(StringBuilder builder, DiffBlock<Tokenized> block, int lineIndex, 
			@Nullable List<DiffBlock<TextToken>> tokenDiffs) {
		builder.append("<div style='").append(getLineOperationStyle(Operation.INSERT)).append("'>+");
		if (tokenDiffs != null) {
			if (tokenDiffs.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (DiffBlock<TextToken> tokenBlock: tokenDiffs) { 
					for (TextToken token: tokenBlock.getUnits()) {
						if (tokenBlock.getOperation() != Operation.DELETE) 
							builder.append(TokenUtils.toHtml(token, null, getTokenOperationStyle(tokenBlock.getOperation())));
					}
				}
			}			
		} else {
			appendTokenized(builder, block.getUnits().get(lineIndex));
		}
		builder.append("</div>");
	}
	
	private static void appendDelete(StringBuilder builder, DiffBlock<Tokenized> block, int lineIndex, 
			@Nullable List<DiffBlock<TextToken>> tokenDiffs) {
		builder.append("<div style='").append(getLineOperationStyle(Operation.DELETE)).append("'>-");
		if (tokenDiffs != null) {
			if (tokenDiffs.isEmpty()) {
				builder.append("&nbsp;");
			} else {
				for (DiffBlock<TextToken> tokenBlock: tokenDiffs) { 
					for (TextToken token: tokenBlock.getUnits()) {
						if (tokenBlock.getOperation() != Operation.INSERT) 
							builder.append(TokenUtils.toHtml(token, null, getTokenOperationStyle(tokenBlock.getOperation())));
					}
				}
			}
		} else {
			appendTokenized(builder, block.getUnits().get(lineIndex));
		}
		builder.append("</div>");
	}
	
	private static void appendModification(StringBuilder builder, DiffBlock<Tokenized> deleteBlock, 
			DiffBlock<Tokenized> insertBlock, int deleteLineIndex, int insertLineIndex, 
			List<DiffBlock<TextToken>> tokenDiffs) {
		builder.append("<div>*");
		if (tokenDiffs.isEmpty()) {
			builder.append("&nbsp;");
		} else {
			for (DiffBlock<TextToken> tokenBlock: tokenDiffs) { 
				for (TextToken token: tokenBlock.getUnits()) 
					builder.append(TokenUtils.toHtml(token, null, getTokenOperationStyle(tokenBlock.getOperation())));
			}
		}
		builder.append("</div>");
	}
	
	private static String getLineOperationStyle(Operation operation) {
		if (operation == Operation.INSERT)
			return "background: #C5F7C5;";
		else if (operation == Operation.DELETE)
			return "background: #FAC8C8;";
		else 
			return null;
	}
	
	private static String getTokenOperationStyle(Operation operation) {
		if (operation == Operation.INSERT)
			return "background: #05AB05; color: white;";
		else if (operation == Operation.DELETE)
			return "background: #F13B3B; color: white; text-decoration: line-through;";
		else 
			return null;
	}
	
	public static String diffAsHtml(List<String> oldLines, @Nullable String oldFileName, 
			List<String> newLines, @Nullable String newFileName, boolean forceAlign) {
		List<DiffBlock<Tokenized>> diffBlocks = DiffUtils.diff(oldLines, oldFileName, newLines, newFileName, 
				WhitespaceOption.DO_NOT_IGNORE);
		StringBuilder builder = new StringBuilder("<div style='font-family: monospace;'>");
		for (int i=0; i<diffBlocks.size(); i++) {
			DiffBlock<Tokenized> block = diffBlocks.get(i);
			if (block.getOperation() == Operation.EQUAL) {
				builder.append("<div>&nbsp;");
				for (int j=0; j<block.getUnits().size(); j++)
					appendTokenized(builder, block.getUnits().get(j));
				builder.append("</div>");
			} else if (block.getOperation() == Operation.DELETE) {
				if (i+1<diffBlocks.size()) {
					DiffBlock<Tokenized> nextBlock = diffBlocks.get(i+1);
					if (nextBlock.getOperation() == Operation.INSERT) {
						LinkedHashMap<Integer, LineDiff> lineChanges = 
								DiffUtils.align(block.getUnits(), nextBlock.getUnits(), forceAlign);
						int prevDeleteLineIndex = 0;
						int prevInsertLineIndex = 0;
						for (Map.Entry<Integer, LineDiff> entry: lineChanges.entrySet()) {
							int deleteLineIndex = entry.getKey();
							LineDiff lineChange = entry.getValue();
							int insertLineIndex = lineChange.getCompareLine();
							
							appendDeletesAndInserts(builder, block, nextBlock, prevDeleteLineIndex, deleteLineIndex, 
									prevInsertLineIndex, insertLineIndex);
							
							appendModification(builder, block, nextBlock, deleteLineIndex, insertLineIndex, 
									lineChange.getTokenDiffs()); 
							
							prevDeleteLineIndex = deleteLineIndex+1;
							prevInsertLineIndex = insertLineIndex+1;
						}
						appendDeletesAndInserts(builder, block, nextBlock, 
								prevDeleteLineIndex, block.getUnits().size(), 
								prevInsertLineIndex, nextBlock.getUnits().size());
						i++;
					} else {
						for (int j=0; j<block.getUnits().size(); j++) 
							appendDelete(builder, block, j, null);
					}
				} else {
					for (int j=0; j<block.getUnits().size(); j++) 
						appendDelete(builder, block, j, null);
				}
			} else {
				for (int j=0; j<block.getUnits().size(); j++) 
					appendInsert(builder, block, j, null);
			}
		}
		builder.append("</div>");
		return builder.toString();
	}
	
}
