package com.pmease.commons.util.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.diff.DiffMatchPatch.Diff;
import com.pmease.commons.util.diff.DiffUnit.Action;

public class DiffUtils {

	/**
	 * Diff two list of strings.
	 * 
	 * @param original 
	 * 			original list of tokens. 
	 * @param revised 
	 * 			revised list of tokens
	 * @return
	 *			list of difference tokens 				
	 */
	public static List<DiffUnit> diff(List<String> original, List<String> revised) {
		Preconditions.checkArgument(original.size() + revised.size() <= 65535, 
				"Total size of original and revised list should be less than 65535.");
		
		DiffMatchPatch dmp = new DiffMatchPatch();
		TokensToCharsResult result = tokensToChars(original, revised);
		
		List<DiffMatchPatch.Diff> diffs = dmp.diff_main(result.chars1, result.chars2, false);

		return charsToTokens(diffs, result.tokenArray);
	}
	
	public static List<DiffChunk> diffAsChunks(List<String> original, List<String> revised, int chunkMargin) {
		return asChunks(diff(original, revised), chunkMargin);
	}

	public static List<DiffChunk> asChunks(List<DiffUnit> diffUnits, int chunkMargin) {
		List<RemoveAwareDiffUnit> removeAwareDiffUnits = new ArrayList<>();
		for (DiffUnit diffUnit: diffUnits)
			removeAwareDiffUnits.add(new RemoveAwareDiffUnit(diffUnit, false));
		
		int equalCount = 0;
		int index = 0;
		for (RemoveAwareDiffUnit each: removeAwareDiffUnits) {
			if (each.diffUnit.getAction() == Action.EQUAL) {
				equalCount++;
			} else {
				for (int i=index-equalCount+chunkMargin; i<=index-chunkMargin-1; i++)
					removeAwareDiffUnits.get(i).removed = true;
				equalCount = 0;
			}
			index++;
		}
		for (int i=index-equalCount+chunkMargin; i<=index-chunkMargin-1; i++)
			removeAwareDiffUnits.get(i).removed = true;
		
		List<DiffChunk> diffChunks = new ArrayList<>();

		int start1 = 0;
		int start2 = 0;
		DiffChunkBuilder chunkBuilder = new DiffChunkBuilder();
		for (RemoveAwareDiffUnit each: removeAwareDiffUnits) {
			if (!each.removed) {
				if (chunkBuilder.diffUnits.isEmpty()) {
					chunkBuilder.start1 = start1;
					chunkBuilder.start2 = start2;
				}
				chunkBuilder.diffUnits.add(each.diffUnit);
			} else {
				if (!chunkBuilder.diffUnits.isEmpty()) {
					diffChunks.add(chunkBuilder.build());
					chunkBuilder = new DiffChunkBuilder();
				}
			}
			if (each.diffUnit.getAction() == Action.INSERT) {
				start2++;
			} else if (each.diffUnit.getAction() == Action.DELETE) {
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
				if (chunkBuilder != null) chunks.add(chunkBuilder.build());
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
			} else if (chunkBuilder != null) {
				if (line.startsWith("+")) {
					chunkBuilder.diffUnits.add(new DiffUnit(Action.INSERT, line.substring(1)));
				} else if (line.startsWith("-")) {
					chunkBuilder.diffUnits.add(new DiffUnit(Action.DELETE, line.substring(1)));
				} else if (line.startsWith(" ")) {
					chunkBuilder.diffUnits.add(new DiffUnit(Action.EQUAL, line.substring(1)));
				} else if (line.startsWith("\\") && !chunkBuilder.diffUnits.isEmpty()) {
					int index = chunkBuilder.diffUnits.size() - 1;
					DiffUnit prevDiff = chunkBuilder.diffUnits.get(index);
					String warnings;
					if (prevDiff.getWarnings() != null)
						warnings = prevDiff.getWarnings() + "\n" + line.substring(2);
					else
						warnings = line.substring(2);
					chunkBuilder.diffUnits.remove(index);
					chunkBuilder.diffUnits.add(new DiffUnit(prevDiff.getAction(), prevDiff.getText(), warnings));
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
	
	private static List<DiffUnit> charsToTokens(List<Diff> diffs, List<String> tokenArray) {
		List<DiffUnit> diffTokens = new ArrayList<>();
		for (Diff diff : diffs) {
			for (int i = 0; i < diff.text.length(); i++) {
				diffTokens.add(new DiffUnit(Action.fromOperation(diff.operation), 
						tokenArray.get(diff.text.charAt(i))));
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
		
		private List<DiffUnit> diffUnits = new ArrayList<>();
		
		private DiffChunk build() {
			return new DiffChunk(start1, start2, diffUnits);
		}
	}
	
	private static class RemoveAwareDiffUnit {
		
		private DiffUnit diffUnit;
		
		private boolean removed;
		
		private RemoveAwareDiffUnit(DiffUnit diffUnit, boolean removed) {
			this.diffUnit = diffUnit;
			this.removed = removed;
		}
	}
}
