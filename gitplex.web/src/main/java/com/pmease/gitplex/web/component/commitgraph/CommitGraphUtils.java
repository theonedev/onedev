package com.pmease.gitplex.web.component.commitgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.GitPlex;

public class CommitGraphUtils {
	
	public static void sort(List<Commit> commits, int from) {
		final Map<String, Long> hash2index = new HashMap<>();
		Map<String, Commit> hash2commit = new HashMap<>();
		for (int i=0; i<commits.size(); i++) {
			Commit commit = commits.get(i);
			hash2index.put(commit.getHash(), 1L*i*commits.size());
			hash2commit.put(commit.getHash(), commit);
		}

		Stack<Commit> stack = new Stack<>();
		
		for (int i=commits.size()-1; i>=from; i--)
			stack.push(commits.get(i));

		// commits are nearly ordered, so this should be fast
		while (!stack.isEmpty()) {
			Commit commit = stack.pop();
			long commitIndex = hash2index.get(commit.getHash());
			int count = 1;
			for (String parentHash: commit.getParentHashes()) {
				Long parentIndex = hash2index.get(parentHash);
				if (parentIndex != null && parentIndex.longValue()<commitIndex) {
					stack.push(hash2commit.get(parentHash));
					hash2index.put(parentHash, commitIndex+(count++));
				}
			}
		}
		
		commits.sort((o1, o2) -> {
			long value = hash2index.get(o1.getHash()) - hash2index.get(o2.getHash());
			if (value < 0)
				return -1;
			else if (value > 0)
				return 1;
			else
				return 0;
		});
	}
	
	public static String asJSON(List<Commit> commits) {
		Map<String, Integer> hash2index = new HashMap<>();
		int commitIndex = 0;
		for (int i=0; i<commits.size(); i++) { 
			Commit commit = commits.get(i);
			if (commit != null)
				hash2index.put(commit.getHash(), commitIndex++);
		}
		List<List<Integer>> commitIndexes = new ArrayList<>();
		for (Commit commit: commits) {
			if (commit != null) {
				List<Integer> parentIndexes = new ArrayList<>();
				for (String parentHash: commit.getParentHashes()) {
					Integer parentIndex = hash2index.get(parentHash);
					if (parentIndex != null)
						parentIndexes.add(parentIndex);
				}
				commitIndexes.add(parentIndexes);
			}
		}
		try {
			return GitPlex.getInstance(ObjectMapper.class).writeValueAsString(commitIndexes);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
