package com.pmease.commons.util.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.Triple;
import com.pmease.commons.util.diff.DiffLine.Action;
import com.pmease.commons.util.diff.DiffMatchPatch.Diff;

public class DiffUtils {

	/**
	 * Diff two list of strings.
	 * 
	 * @param original 
	 * 			original list of tokens. 
	 * @param revised 
	 * 			revised list of tokens
	 * @param tokenSplitter
	 * 			pass a not null token splitter to split line into tokens, in order to 
	 * 			identify modified lines in diff result, so that modified tokens can be 
	 * 			marked via {@link Token#isEmphasized()} 
	 * @return
	 *			list of diff lines 				
	 */
	public static List<DiffLine> diff(List<String> original, List<String> revised, 
			@Nullable TokenSplitter tokenSplitter) {
		Preconditions.checkArgument(original.size() + revised.size() <= 65535, 
				"Total size of original and revised list should be less than 65535.");
		DiffMatchPatch dmp = new DiffMatchPatch();
		TokensToCharsResult result = tokensToChars(original, revised);
		
		List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result.chars1, result.chars2, false);

		List<DiffLine> diffLines = charsToTokens(diffs, result.tokenArray);
		
		if (tokenSplitter != null)
			return diffTokens(diffLines, tokenSplitter);
		else 
			return diffLines;
	}

	public static List<DiffLine> diffTokens(List<DiffLine> diffs, TokenSplitter tokenSplitter) {
		List<DiffLine> processedDiffs = new ArrayList<>();
		List<Triple<String, Integer, Integer>> deletions = new ArrayList<>();
		List<Triple<String, Integer, Integer>> additions = new ArrayList<>();
		List<DiffLine> processedDeletions = new ArrayList<>();
		List<DiffLine> processedAdditions = new ArrayList<>();
		for (DiffLine diffLine: diffs) {
			Preconditions.checkState(diffLine.getTokens().size() == 1);
			if (diffLine.getAction() == DiffLine.Action.DELETE) {
				deletions.add(new Triple<String, Integer, Integer>(
						diffLine.getTokens().get(0).getContent(), diffLine.getOldLineNo(), diffLine.getNewLineNo()));
			} else if (diffLine.getAction() == DiffLine.Action.ADD) {
				additions.add(new Triple<String, Integer, Integer>(
						diffLine.getTokens().get(0).getContent(), diffLine.getOldLineNo(), diffLine.getNewLineNo()));
			} else {
				process(processedDiffs, deletions, additions, processedDeletions, processedAdditions, tokenSplitter);
				processedDiffs.add(new DiffLine(DiffLine.Action.EQUAL, diffLine.getTokens().get(0).getContent(), 
						diffLine.getOldLineNo(), diffLine.getNewLineNo()));
			}
		}
		process(processedDiffs, deletions, additions, processedDeletions, processedAdditions, tokenSplitter);
		
		return processedDiffs;
	}
	
	private static List<String> getTokens(String line, TokenSplitter splitter, Map<String, List<String>> tokensCache) {
		List<String> tokens = tokensCache.get(line);
		if (tokens == null) {
			tokens = splitter.split(line);
			tokensCache.put(line, tokens);
		}
		return tokens;
	}
	
	private static void process(List<DiffLine> processedDiffLines, List<Triple<String, Integer, Integer>> deletions, 
			List<Triple<String, Integer, Integer>> additions, List<DiffLine> processedDeletions, 
			List<DiffLine> processedAdditions, TokenSplitter tokenSplitter) {
		Map<String, List<String>> tokensCache = new HashMap<String, List<String>>();
		
		for (Triple<String, Integer, Integer> deletion: deletions) {
			boolean matching = false;
			for (int i=0; i<additions.size(); i++) {
				Triple<String, Integer, Integer> addition = additions.get(i);
				
				List<DiffLine> diffTokens = diff(getTokens(deletion.getFirst(), tokenSplitter, tokensCache), 
						getTokens(addition.getFirst(), tokenSplitter, tokensCache), null);
				int equals = 0;
				int total = 0;
				for (DiffLine diffToken: diffTokens) {
					if (StringUtils.isNotBlank(diffToken.getTokens().get(0).getContent())) {
						total ++;
						if (diffToken.getAction() == DiffLine.Action.EQUAL)
							equals++;
					}
				}
				if (equals*3 >= total) {
					for (int j=0; j<i; j++)	{
						Triple<String, Integer, Integer> prevAddition = additions.get(j);
						processedAdditions.add(new DiffLine(DiffLine.Action.ADD, prevAddition.getFirst(), 
								prevAddition.getSecond(), prevAddition.getThird()));
					}
					for (int j=0; j<=i; j++)
						additions.remove(0);
					List<Token> addTokens = new ArrayList<>();
					List<Token> deleteTokens = new ArrayList<>();
					for (DiffLine diffToken: diffTokens) {
						Preconditions.checkState(diffToken.getTokens().size() == 1);
						if (diffToken.getAction() == DiffLine.Action.ADD) {
							addTokens.add(new Token(diffToken.getTokens().get(0).getContent(), true));
						} else if (diffToken.getAction() == DiffLine.Action.DELETE) {
							deleteTokens.add(new Token(diffToken.getTokens().get(0).getContent(), true));
						} else {
							addTokens.add(new Token(diffToken.getTokens().get(0).getContent(), false));
							deleteTokens.add(new Token(diffToken.getTokens().get(0).getContent(), false));
						}
					}
					processedAdditions.add(new DiffLine(DiffLine.Action.ADD, addTokens, 
							addition.getSecond(), addition.getThird()));
					processedDeletions.add(new DiffLine(DiffLine.Action.DELETE, deleteTokens, 
							deletion.getSecond(), deletion.getThird()));
					matching = true;
					break;
				}
			}
			if (!matching) { 
				processedDeletions.add(new DiffLine(DiffLine.Action.DELETE, 
						deletion.getFirst(), deletion.getSecond(), deletion.getThird()));
			}
		}
		
		for (Triple<String, Integer, Integer> addition: additions) {
			processedAdditions.add(new DiffLine(DiffLine.Action.ADD, 
					addition.getFirst(), addition.getSecond(), addition.getThird()));
		}
		
		additions.clear();
		deletions.clear();
		processedDiffLines.addAll(processedDeletions);
		processedDiffLines.addAll(processedAdditions);
		processedDeletions.clear();
		processedAdditions.clear();
	}
	
	public static Map<Integer, Integer> mapLines(List<String> original, List<String> revised) {
		return mapLines(diff(original, revised, null));
	}
	
	public static Map<Integer, Integer> mapLines(List<DiffLine> diffs) {
		Map<Integer, Integer> lineMapping = new HashMap<Integer, Integer>();
		int originalLine = 0;
		int revisedLine = 0;
		for (DiffLine diff: diffs) {
			if (diff.getAction() == DiffLine.Action.ADD) {
				revisedLine++;
			} else if (diff.getAction() == DiffLine.Action.DELETE) {
				originalLine++;
			} else {
				lineMapping.put(originalLine, revisedLine);
				revisedLine++;
				originalLine++;
			}
		}
		return lineMapping;
	}

	public static AroundContext around(List<DiffLine> diffs, int oldLine, int newLine, int contextSize) {
		List<DiffLine> contextDiffs = new ArrayList<>();
		int index = -1;
		for (int i=0; i<diffs.size(); i++) {
			DiffLine diff = diffs.get(i);
			if (diff.getOldLineNo() == oldLine || diff.getNewLineNo() == newLine) {
				index = i;
				break;
			}
		}
		
		Preconditions.checkState(index != -1);
		
		int start = index - contextSize;
		if (start < 0)
			start = 0;
		int end = index + contextSize;
		if (end > diffs.size() - 1)
			end = diffs.size() - 1;
		
		for (int i=start; i<=end; i++)
			contextDiffs.add(diffs.get(i));
		
		return new AroundContext(contextDiffs, index-start, start>0, end<diffs.size()-1);
	}
	
	public static List<DiffHunk> diffAsHunks(List<String> original, List<String> revised, 
			TokenSplitter wordSplitter, int contextSize) {
		return hunksOf(diff(original, revised, wordSplitter), contextSize);
	}

	public static List<DiffHunk> diffAsHunks(List<String> original, List<String> revised, 
			TokenSplitter wordSplitter, Set<Integer> additionalOldLinesToPreserve, 
			Set<Integer> additionalNewLinesToPreserve, int contextSize) {
		return hunksOf(diff(original, revised, wordSplitter), additionalOldLinesToPreserve, 
				additionalNewLinesToPreserve, contextSize);
	}

	public static List<DiffHunk> hunksOf(List<DiffLine> diffLines, int contextSize) {
		return hunksOf(diffLines, new HashSet<Integer>(), new HashSet<Integer>(), contextSize);
	}
	
	public static List<DiffHunk> hunksOf(List<DiffLine> diffLines, Set<Integer> additionalOldLinesToPreserve, 
			Set<Integer> additionalNewLinesToPreserve, int contextSize) {
		List<RemovableDiffLine> removableDiffLines = new ArrayList<>();
		for (DiffLine diffLine: diffLines)
			removableDiffLines.add(new RemovableDiffLine(diffLine, false));
		
		int contextCount = 0;
		int index = 0;
		for (RemovableDiffLine each: removableDiffLines) {
			if (each.diffLine.getAction() == Action.EQUAL 
					&& !additionalOldLinesToPreserve.contains(each.diffLine.getOldLineNo())
					&& !additionalNewLinesToPreserve.contains(each.diffLine.getNewLineNo())) {
				contextCount++;
			} else if (index == contextCount) {
				for (int i=index-contextCount; i<index-contextSize; i++)
					removableDiffLines.get(i).removed = true;
				contextCount = 0;
			} else {
				for (int i=index-contextCount+contextSize; i<index-contextSize; i++)
					removableDiffLines.get(i).removed = true;
				contextCount = 0;
			}
			index++;
		}
		for (int i=index-contextCount+contextSize; i<index; i++)
			removableDiffLines.get(i).removed = true;
		
		List<DiffHunk> hunks = new ArrayList<>();

		int oldStart = 0;
		int newStart = 0;
		DiffHunkBuilder hunkBuilder = new DiffHunkBuilder();
		for (RemovableDiffLine each: removableDiffLines) {
			if (!each.removed) {
				if (hunkBuilder.diffLines.isEmpty()) {
					hunkBuilder.oldStart = oldStart;
					hunkBuilder.newStart = newStart;
				}
				hunkBuilder.diffLines.add(each.diffLine);
			} else {
				if (!hunkBuilder.diffLines.isEmpty()) {
					hunks.add(hunkBuilder.build());
					hunkBuilder = new DiffHunkBuilder();
				}
			}
			if (each.diffLine.getAction() == Action.ADD) {
				newStart++;
			} else if (each.diffLine.getAction() == Action.DELETE) {
				oldStart++;
			} else {
				oldStart++; 
				newStart++;
			}
		}
		if (!hunkBuilder.diffLines.isEmpty())
			hunks.add(hunkBuilder.build());
		
		return hunks;
	}
	
	public static List<DiffHunk> parseUnifiedDiff(List<String> unifiedDiff) {
		List<DiffHunk> hunks = new ArrayList<>();

		DiffHunkBuilder hunkBuilder = null;
		for (String line : unifiedDiff) {
			if (line.startsWith("@@")) {
				if (hunkBuilder != null) 
					hunks.add(hunkBuilder.build());
				hunkBuilder = new DiffHunkBuilder();
				line = StringUtils.substringBefore(line.substring(4), " @@");
				String first = StringUtils.substringBefore(line, " +");
				String second = StringUtils.substringAfter(line, " +");
				if (first.indexOf(",") != -1) {
					hunkBuilder.oldStart = Integer.parseInt(StringUtils.substringBefore(first, ",")) - 1;
				} else {
					hunkBuilder.oldStart = Integer.parseInt(first) - 1;
				}
				if (second.indexOf(",") != -1) {
					hunkBuilder.newStart = Integer.parseInt(StringUtils.substringBefore(second, ",")) - 1;
				} else {
					hunkBuilder.newStart = Integer.parseInt(second) - 1;
				}
				hunkBuilder.current1 = hunkBuilder.oldStart;
				hunkBuilder.current2 = hunkBuilder.newStart;
			} else if (hunkBuilder != null) {
				if (line.startsWith("+")) {
					hunkBuilder.diffLines.add(new DiffLine(Action.ADD, line.substring(1), 
							hunkBuilder.current1, hunkBuilder.current2));
					hunkBuilder.current2++;
				} else if (line.startsWith("-")) {
					hunkBuilder.diffLines.add(new DiffLine(Action.DELETE, line.substring(1), 
							hunkBuilder.current1, hunkBuilder.current2));
					hunkBuilder.current1++;
				} else if (line.startsWith(" ")) {
					hunkBuilder.diffLines.add(new DiffLine(Action.EQUAL, line.substring(1), 
							hunkBuilder.current1, hunkBuilder.current2));
					hunkBuilder.current1++;
					hunkBuilder.current2++;
				}
			}
		}

		if (hunkBuilder != null) hunks.add(hunkBuilder.build());

		return hunks;
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
		return new TokensToCharsResult(chars1, chars2, tokenArray);
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
	
	private static List<DiffLine> charsToTokens(List<Diff> diffs, List<String> tokenArray) {
		List<DiffLine> diffTokens = new ArrayList<>();
		int oldLineNo = 0;
		int newLineNo = 0;
		for (Diff diff : diffs) {
			for (int i = 0; i < diff.text.length(); i++) {
				Action action = Action.fromOperation(diff.operation);
				diffTokens.add(new DiffLine(Action.fromOperation(diff.operation), 
						tokenArray.get(diff.text.charAt(i)), oldLineNo, newLineNo));
				if (action == Action.ADD) {
					newLineNo ++;
				} else if (action == Action.DELETE) {
					oldLineNo ++;
				} else {
					oldLineNo ++;
					newLineNo ++;
				}
			}
		}
		return diffTokens;
	}
	
	private static class TokensToCharsResult {
		private String chars1;
		private String chars2;
		private List<String> tokenArray;

		private TokensToCharsResult(String chars1, String chars2, List<String> tokenArray) {
			this.chars1 = chars1;
			this.chars2 = chars2;
			this.tokenArray = tokenArray;
		}
	}

	private static class DiffHunkBuilder {
		private int oldStart, newStart;
		
		private int current1, current2;
		
		private List<DiffLine> diffLines = new ArrayList<>();
		
		private DiffHunk build() {
			return new DiffHunk(oldStart, newStart, diffLines);
		}
	}
	
}
