package io.onedev.server.util.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.diff.DiffMatchPatch.Diff;
import io.onedev.server.util.diff.DiffMatchPatch.Operation;

public class DiffUtils {

	public static final int MAX_DIFF_SIZE = 65535;
	
	public static final int MAX_LINE_LEN = 10000;
	
	private static final Pattern WORD_PATTERN = Pattern.compile("\\w+");
	
	public static List<String> getLines(@Nullable String text) {
		List<String> lines = new ArrayList<>();
		if (text != null)
			lines = Splitter.on("\n").splitToList(text);
		else
			lines = new ArrayList<>();
		return lines;
	}
	
	private static List<String> getTokens(String line) {
		List<String> tokens = new ArrayList<>();
		Matcher matcher = WORD_PATTERN.matcher(line);
		int lastEnd = 0;
		while (matcher.find()) {
			int start = matcher.start();
			if (start > lastEnd) {
				for (int i=lastEnd; i<start; i++)
					tokens.add(String.valueOf(line.charAt(i)));
			}
            tokens.add(line.substring(matcher.start(), matcher.end()));
            lastEnd = matcher.end();
        }
		if (lastEnd < line.length()) {
			for (int i=lastEnd; i<line.length(); i++)
				tokens.add(String.valueOf(line.charAt(i)));
		}
		return tokens;
	}
	
	public static <T> List<DiffBlock<T>> diff(List<T> oldElements, List<T> newElements, Function<T, T> processor) {
		Preconditions.checkArgument(oldElements.size() + newElements.size() <= MAX_DIFF_SIZE, 
				"Total size of old lines and new lines should be less than " + MAX_DIFF_SIZE + ".");
		
		List<T> processedOldElements = new ArrayList<>();
		for (T element: oldElements) 
			processedOldElements.add(processor.apply(element));
		
		List<T> processedNewElements = new ArrayList<>();
		for (T element: newElements) 
			processedNewElements.add(processor.apply(element));
		
		DiffMatchPatch dmp = new DiffMatchPatch();
		TokensToCharsResult<T> result1 = tokensToChars(processedOldElements, processedNewElements);
		
		List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result1.chars1, result1.chars2, false);

		List<DiffBlock<T>> diffBlocks = new ArrayList<>();
		int oldElementIndex = 0;
		int newElementIndex = 0;
		for (Diff diff: diffs) {
			List<T> elements = new ArrayList<>();
			if (diff.operation == Operation.EQUAL) {
				for (int i = 0; i < diff.text.length(); i++) {
					elements.add(newElements.get(newElementIndex));
					oldElementIndex++;
					newElementIndex++;
				}
				diffBlocks.add(new DiffBlock<>(diff.operation, elements, 
						oldElementIndex-elements.size(), newElementIndex-elements.size()));
			} else if (diff.operation == Operation.INSERT) {
				for (int i = 0; i < diff.text.length(); i++)
					elements.add(newElements.get(newElementIndex++));
				diffBlocks.add(new DiffBlock<>(diff.operation, elements, 
						oldElementIndex, newElementIndex-elements.size()));
			} else {
				for (int i = 0; i < diff.text.length(); i++)
					elements.add(oldElements.get(oldElementIndex++));
				diffBlocks.add(new DiffBlock<>(diff.operation, elements, 
						oldElementIndex-elements.size(), newElementIndex));
			}
		}
		
		return diffBlocks;
	}
	
	public static <T> List<DiffBlock<T>> diff(List<T> oldLines, List<T> newLines) {
		return diff(oldLines, newLines, new Function<T, T>() {

			@Override
			public T apply(T t) {
				return t;
			}
			
		});
	}
	
	public static LinkedHashMap<Integer, List<DiffBlock<String>>> diffLines(List<String> deleteLines, List<String> insertLines) {
		LinkedHashMap<Integer, List<DiffBlock<String>>> lineDiffs = new LinkedHashMap<>();
		
		DiffMatchPatch dmp = new DiffMatchPatch();
		
		for (int i=0; i<deleteLines.size(); i++) {
			String deleteLine = deleteLines.get(i);
			List<String> deleteTokens = getTokens(deleteLine);
			if (i < insertLines.size()) {
				String insertLine = insertLines.get(i);
				List<String> insertTokens = getTokens(insertLine);
				
				TokensToCharsResult<String> result = DiffUtils.tokensToChars(deleteTokens, insertTokens);						
				List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result.chars1, result.chars2, false);
				int equal = 0;
				int total = 0;
				for (DiffMatchPatch.Diff diff: diffs) {
					for (int k=0; k<diff.text.length(); k++) {
						int pos = diff.text.charAt(k);
						String token = result.tokenArray.get(pos);
						if (StringUtils.isNotBlank(token)) {
							total += token.length();
							if (diff.operation == Operation.EQUAL)
								equal += token.length();
						}
					}
				}
				if (equal*3 >= total) {
					List<DiffBlock<String>> diffBlocks = new ArrayList<>();
					int oldLineNo = 0;
					int newLineNo = 0;
					for (Diff diff : diffs) {
						List<String> tokens = new ArrayList<>();
						if (diff.operation == Operation.EQUAL) {
							for (int k = 0; k < diff.text.length(); k++) {
								tokens.add(insertTokens.get(newLineNo));
								oldLineNo++;
								newLineNo++;
							}
							diffBlocks.add(new DiffBlock<String>(
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

					lineDiffs.put(i, diffBlocks);
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
	
	public static <T> Map<Integer, Integer> mapLines(List<DiffBlock<T>> diffBlocks) {
		Map<Integer, Integer> lineMapping = new HashMap<Integer, Integer>();
		for (DiffBlock<T> diffBlock: diffBlocks) {
			if (diffBlock.getOperation() == Operation.EQUAL) {
				for (int i=0; i<diffBlock.getElements().size(); i++)
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
