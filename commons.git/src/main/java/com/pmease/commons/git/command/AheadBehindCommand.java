package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.AheadBehind;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class AheadBehindCommand extends GitCommand<Map<String, AheadBehind>> {

	private static final Logger logger = LoggerFactory.getLogger(AheadBehindCommand.class);
	
	private String baseRev;

	private String[] compareRevs;
	
	public AheadBehindCommand(File repoDir) {
		super(repoDir);
	}

	public AheadBehindCommand baseCommit(String baseRev) {
		this.baseRev = Preconditions.checkNotNull(baseRev);
		return this;
	}
	
	public AheadBehindCommand compareRevs(String... compareRevs) {
		this.compareRevs = Preconditions.checkNotNull(compareRevs);
		return this;
	}
	
	@Override
	public Map<String, AheadBehind> call() {
		Preconditions.checkNotNull(baseRev);
		Preconditions.checkArgument(compareRevs != null);
		
		if (compareRevs.length > 1) {
			final List<String> nonHashRevs = new ArrayList<>();
			final Map<String, String> revHashes = new HashMap<>();
			if (GitUtils.isHash(baseRev))
				revHashes.put(baseRev, baseRev);
			else
				nonHashRevs.add(baseRev);
			for (String each: compareRevs) {
				if (GitUtils.isHash(each))
					revHashes.put(each, each);
				else
					nonHashRevs.add(each);
			}
			
			Commandline cmd = cmd();
			cmd.addArgs("rev-parse");
			for (String each: nonHashRevs)
				cmd.addArgs(each);
			
			final AtomicInteger index = new AtomicInteger(0);
			cmd.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					revHashes.put(nonHashRevs.get(index.getAndIncrement()), line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.error(line);
				}
				
			}).checkReturnCode();

			cmd = cmd();
			cmd.addArgs("rev-list", "--parents", "^" + revHashes.get(baseRev));
			for (String each: compareRevs)
				cmd.addArgs(revHashes.get(each));
			
			final Map<String, Set<String>> commits = new HashMap<>();
			cmd.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					parseCommitLine(commits, line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.error(line);
				}
				
			}).checkReturnCode();
			
			Map<String, AheadBehind> aheadBehinds = new HashMap<>();
			for (String each: compareRevs) {
				AheadBehind ab = new AheadBehind();
				ab.setAhead(findAncestors(commits, revHashes.get(each)).size());
				aheadBehinds.put(each, ab);
			}
			
			cmd = cmd();
			cmd.addArgs("merge-base", "--all", "--octopus");
			for (String each: compareRevs)
				cmd.addArgs(revHashes.get(each));
			
			final Set<String> mergeBases = new HashSet<>();
			cmd.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					while (tokenizer.hasMoreTokens())
						mergeBases.add(tokenizer.nextToken());
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.error(line);
				}
				
			}).checkReturnCode();
			
			cmd = cmd();
			cmd.addArgs("rev-list");
			for (String each: mergeBases)
				cmd.addArgs("^" + each);
			
			cmd.addArgs(baseRev);

			final Set<String> mergeBaseBehindBase = new HashSet<>();
			cmd.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					mergeBaseBehindBase.add(line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.error(line);
				}
				
			}).checkReturnCode();
			
			cmd = cmd();
			cmd.addArgs("rev-list", "--parents");
			for (String each: mergeBases)
				cmd.addArgs("^" + each);
			for (String each: compareRevs)
				cmd.addArgs(revHashes.get(each));
			
			commits.clear();
			cmd.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					parseCommitLine(commits, line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.error(line);
				}
				
			}).checkReturnCode();
			
			for (Map.Entry<String, AheadBehind> entry: aheadBehinds.entrySet()) {
				String branchHash = revHashes.get(entry.getKey());
				Set<String> copyOfMergeBaseBehindBase = new HashSet<>(mergeBaseBehindBase);
				copyOfMergeBaseBehindBase.removeAll(findAncestors(commits, branchHash));
				entry.getValue().setBehind(copyOfMergeBaseBehindBase.size());
			}
			
			return aheadBehinds;
		} else if (compareRevs.length == 1) {
			Commandline cmd = cmd();
			cmd.addArgs("rev-list", "--left-right", "--count");
			cmd.addArgs(baseRev + "..." + compareRevs[0]);

			final AheadBehind aheadBehind = new AheadBehind();
			cmd.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					aheadBehind.setBehind(Integer.parseInt(tokenizer.nextToken()));
					aheadBehind.setAhead(Integer.parseInt(tokenizer.nextToken()));
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.error(line);
				}
				
			}).checkReturnCode();			
			
			Map<String, AheadBehind> aheadBehinds = new HashMap<>();
			aheadBehinds.put(compareRevs[0], aheadBehind);
			return aheadBehinds;
		} else {
			return new HashMap<String, AheadBehind>();
		}
	}
	
	private void parseCommitLine(Map<String, Set<String>> commits, String line) {
		Set<String> parents = new HashSet<>();
		StringTokenizer tokenizer = new StringTokenizer(line);
		String commit = tokenizer.nextToken();
		while (tokenizer.hasMoreTokens())
			parents.add(tokenizer.nextToken());
		commits.put(commit, parents);
	}
	
	private Set<String> findAncestors(Map<String, Set<String>> commits, String commit) {
		Set<String> ancestors = new HashSet<>();
		Stack<String> stack = new Stack<>();
		stack.push(commit);
		while (!stack.isEmpty()) {
			commit = stack.pop();
			if (!ancestors.contains(commit)) {
				Set<String> parents = commits.get(commit);
				if (parents != null) {
					ancestors.add(commit);
					for (String parent: parents)
						stack.push(parent);
				}
			}
		}
		return ancestors;
	}
	
}
