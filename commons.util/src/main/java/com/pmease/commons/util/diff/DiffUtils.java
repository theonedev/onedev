package com.pmease.commons.util.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * @param partialSplitter
	 * 			pass a not null partial splitter to split line into partials, in order to 
	 * 			identify modified lines in diff result, so that modified partials can be 
	 * 			marked via {@link Partial#isEmphasized()} 
	 * @return
	 *			list of difference tokens 				
	 */
	public static List<DiffLine> diff(List<String> original, List<String> revised, 
			@Nullable PartialSplitter partialSplitter) {
		Preconditions.checkArgument(original.size() + revised.size() <= 65535, 
				"Total size of original and revised list should be less than 65535.");
		DiffMatchPatch dmp = new DiffMatchPatch();
		TokensToCharsResult result = tokensToChars(original, revised);
		
		List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result.chars1, result.chars2, false);

		List<DiffLine> diffLines = charsToTokens(diffs, result.tokenArray);
		
		if (partialSplitter != null) {
			List<DiffLine> processedDiffLines = new ArrayList<>();
			List<Triple<String, Integer, Integer>> deletions = new ArrayList<>();
			List<Triple<String, Integer, Integer>> additions = new ArrayList<>();
			List<DiffLine> processedDeletions = new ArrayList<>();
			List<DiffLine> processedAdditions = new ArrayList<>();
			for (DiffLine diffLine: diffLines) {
				Preconditions.checkState(diffLine.getPartials().size() == 1);
				if (diffLine.getAction() == DiffLine.Action.DELETE) {
					deletions.add(new Triple<String, Integer, Integer>(
							diffLine.getPartials().get(0).getContent(), diffLine.getOldLineNo(), diffLine.getNewLineNo()));
				} else if (diffLine.getAction() == DiffLine.Action.ADD) {
					additions.add(new Triple<String, Integer, Integer>(
							diffLine.getPartials().get(0).getContent(), diffLine.getOldLineNo(), diffLine.getNewLineNo()));
				} else {
					process(processedDiffLines, deletions, additions, processedDeletions, processedAdditions, partialSplitter);
					processedDiffLines.add(new DiffLine(DiffLine.Action.EQUAL, diffLine.getPartials().get(0).getContent(), 
							diffLine.getOldLineNo(), diffLine.getNewLineNo()));
				}
			}
			process(processedDiffLines, deletions, additions, processedDeletions, processedAdditions, partialSplitter);
			
			return processedDiffLines;
		} else {
			return diffLines;
		}
	}
	
	private static List<String> getPartials(String line, PartialSplitter splitter, Map<String, List<String>> partialsCache) {
		List<String> partials = partialsCache.get(line);
		if (partials == null) {
			partials = splitter.split(line);
			partialsCache.put(line, partials);
		}
		return partials;
	}
	
	private static void process(List<DiffLine> processedDiffLines, List<Triple<String, Integer, Integer>> deletions, 
			List<Triple<String, Integer, Integer>> additions, List<DiffLine> processedDeletions, 
			List<DiffLine> processedAdditions, PartialSplitter partialSplitter) {
		Map<String, List<String>> partialsCache = new HashMap<String, List<String>>();
		
		for (Triple<String, Integer, Integer> deletion: deletions) {
			boolean matching = false;
			for (int i=0; i<additions.size(); i++) {
				Triple<String, Integer, Integer> addition = additions.get(i);
				
				List<DiffLine> diffPartials = diff(getPartials(deletion.getFirst(), partialSplitter, partialsCache), 
						getPartials(addition.getFirst(), partialSplitter, partialsCache), null);
				int equals = 0;
				for (DiffLine diffPartial: diffPartials) {
					if (diffPartial.getAction() == DiffLine.Action.EQUAL)
						equals++;
				}
				if (equals*3 >= diffPartials.size()) {
					for (int j=0; j<i; j++)	{
						Triple<String, Integer, Integer> prevAddition = additions.get(j);
						processedAdditions.add(new DiffLine(DiffLine.Action.ADD, prevAddition.getFirst(), 
								prevAddition.getSecond(), prevAddition.getThird()));
					}
					for (int j=0; j<=i; j++)
						additions.remove(0);
					List<Partial> addPartials = new ArrayList<>();
					List<Partial> deletePartials = new ArrayList<>();
					for (DiffLine diffPartial: diffPartials) {
						Preconditions.checkState(diffPartial.getPartials().size() == 1);
						if (diffPartial.getAction() == DiffLine.Action.ADD) {
							addPartials.add(new Partial(diffPartial.getPartials().get(0).getContent(), true));
						} else if (diffPartial.getAction() == DiffLine.Action.DELETE) {
							deletePartials.add(new Partial(diffPartial.getPartials().get(0).getContent(), true));
						} else {
							addPartials.add(new Partial(diffPartial.getPartials().get(0).getContent(), false));
							deletePartials.add(new Partial(diffPartial.getPartials().get(0).getContent(), false));
						}
					}
					processedAdditions.add(new DiffLine(DiffLine.Action.ADD, addPartials, 
							addition.getSecond(), addition.getThird()));
					processedDeletions.add(new DiffLine(DiffLine.Action.DELETE, deletePartials, 
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
		Map<Integer, Integer> lineMapping = new HashMap<Integer, Integer>();
		int originalLine = 0;
		int revisedLine = 0;
		for (DiffLine diff: diff(original, revised, null)) {
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
	
	public static List<DiffChunk> diffAsChunks(List<String> original, List<String> revised, 
			PartialSplitter wordSplitter, int chunkMargin) {
		return chunksOf(diff(original, revised, wordSplitter), chunkMargin);
	}

	public static List<DiffChunk> chunksOf(List<DiffLine> diffLines, int contextLines) {
		List<RemovableDiffLine> removableDiffLines = new ArrayList<>();
		for (DiffLine diffLine: diffLines)
			removableDiffLines.add(new RemovableDiffLine(diffLine, false));
		
		int equalCount = 0;
		int index = 0;
		for (RemovableDiffLine each: removableDiffLines) {
			if (each.diffLine.getAction() == Action.EQUAL) {
				equalCount++;
			} else if (index == equalCount) {
				for (int i=index-equalCount; i<index-contextLines; i++)
					removableDiffLines.get(i).removed = true;
				equalCount = 0;
			} else {
				for (int i=index-equalCount+contextLines; i<index-contextLines; i++)
					removableDiffLines.get(i).removed = true;
				equalCount = 0;
			}
			index++;
		}
		for (int i=index-equalCount+contextLines; i<index; i++)
			removableDiffLines.get(i).removed = true;
		
		List<DiffChunk> diffChunks = new ArrayList<>();

		int start1 = 0;
		int start2 = 0;
		DiffChunkBuilder chunkBuilder = new DiffChunkBuilder();
		for (RemovableDiffLine each: removableDiffLines) {
			if (!each.removed) {
				if (chunkBuilder.diffUnits.isEmpty()) {
					chunkBuilder.start1 = start1;
					chunkBuilder.start2 = start2;
				}
				chunkBuilder.diffUnits.add(each.diffLine);
			} else {
				if (!chunkBuilder.diffUnits.isEmpty()) {
					diffChunks.add(chunkBuilder.build());
					chunkBuilder = new DiffChunkBuilder();
				}
			}
			if (each.diffLine.getAction() == Action.ADD) {
				start2++;
			} else if (each.diffLine.getAction() == Action.DELETE) {
				start1++;
			} else {
				start1++; 
				start2++;
			}
		}
		if (!chunkBuilder.diffUnits.isEmpty())
			diffChunks.add(chunkBuilder.build());
		
		return diffChunks;
	}
	
	public static List<DiffChunk> parseUnifiedDiff(List<String> unifiedDiff) {
		List<DiffChunk> chunks = new ArrayList<>();

		DiffChunkBuilder chunkBuilder = null;
		for (String line : unifiedDiff) {
			if (line.startsWith("@@")) {
				if (chunkBuilder != null) 
					chunks.add(chunkBuilder.build());
				chunkBuilder = new DiffChunkBuilder();
				line = StringUtils.substringBefore(line.substring(4), " @@");
				String first = StringUtils.substringBefore(line, " +");
				String second = StringUtils.substringAfter(line, " +");
				if (first.indexOf(",") != -1) {
					chunkBuilder.start1 = Integer.parseInt(StringUtils.substringBefore(first, ",")) - 1;
				} else {
					chunkBuilder.start1 = Integer.parseInt(first) - 1;
				}
				if (second.indexOf(",") != -1) {
					chunkBuilder.start2 = Integer.parseInt(StringUtils.substringBefore(second, ",")) - 1;
				} else {
					chunkBuilder.start2 = Integer.parseInt(second) - 1;
				}
				chunkBuilder.current1 = chunkBuilder.start1;
				chunkBuilder.current2 = chunkBuilder.start2;
			} else if (chunkBuilder != null) {
				if (line.startsWith("+")) {
					chunkBuilder.diffUnits.add(new DiffLine(Action.ADD, line.substring(1), 
							chunkBuilder.current1, chunkBuilder.current2));
					chunkBuilder.current2++;
				} else if (line.startsWith("-")) {
					chunkBuilder.diffUnits.add(new DiffLine(Action.DELETE, line.substring(1), 
							chunkBuilder.current1, chunkBuilder.current2));
					chunkBuilder.current1++;
				} else if (line.startsWith(" ")) {
					chunkBuilder.diffUnits.add(new DiffLine(Action.EQUAL, line.substring(1), 
							chunkBuilder.current1, chunkBuilder.current2));
					chunkBuilder.current1++;
					chunkBuilder.current2++;
				}
			}
		}

		if (chunkBuilder != null) chunks.add(chunkBuilder.build());

		return chunks;
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

	private static class DiffChunkBuilder {
		private int start1, start2;
		
		private int current1, current2;
		
		private List<DiffLine> diffUnits = new ArrayList<>();
		
		private DiffChunk build() {
			return new DiffChunk(start1, start2, diffUnits);
		}
	}
	
}
