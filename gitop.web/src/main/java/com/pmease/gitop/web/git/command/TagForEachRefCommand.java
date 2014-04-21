package com.pmease.gitop.web.git.command;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pmease.commons.git.GitContrib;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.gitop.web.git.GitUtils;

public class TagForEachRefCommand extends ForEachRefCommand<Map<String, Tag>, TagForEachRefCommand> {

	public TagForEachRefCommand(File repoDir) {
		super(repoDir);
	}

	@Override
	protected TagForEachRefCommand self() {
		return this;
	}

	@Override
	protected ForEachRefOutputHandler<Map<String, Tag>> getOutputHandler() {
		return new TagOutputHandler();
	}

	@Override
	protected void applyArgs(Commandline cmd) {
		super.applyArgs(cmd);
		
		if (Strings.isNullOrEmpty(getSort())) {
			cmd.addArgs("--sort=-creatordate");
		}
		
		if (getPatterns().length == 0) {
			cmd.addArgs(Constants.R_TAGS);
		}
	}
	
	static final String BLOCK_SEPARATOR = "-----B-----L-----O-----C-----K-----";
	
	private static final String FORMAT = 
			"%(refname)|" +		// ref name
			"%(objectname)|" +			// ref sha-1
			"%(*objectname)|" +			// 
			"%(creator)|" +
			"%0A%(contents:subject)" +
			"%0A%(contents:body)" +
			"%0A" + BLOCK_SEPARATOR;

	static class TagOutputHandler extends ForEachRefOutputHandler<Map<String, Tag>> {
		final Map<String, Tag> tags = Maps.newLinkedHashMap();

		List<String> contents = Lists.newArrayList();
		
		Tag.Builder builder = Tag.builder();
		
		@Override
		public Map<String, Tag> getResult() {
			return tags;
		}

		@Override
		public void consume(String line) {
			if (Objects.equal(line, BLOCK_SEPARATOR)) {
				onBlockFinish();
				return;
			}
			
			if (line.startsWith(Constants.R_TAGS)) {
				onMeta(line);
				return;
			}
			
			contents.add(line);
		}
		
		void onMeta(String line) {
			String[] pieces = Iterables.toArray(Splitter.on("|").split(line), String.class);
			int i = 0;
			String refname = pieces[i++].substring(Constants.R_TAGS.length());
			builder.name(refname);
			String hash1 = pieces[i++];
			String hash2 = pieces[i++];
			builder.hash(hash1);
			builder.commitHash(hash2);
			
			GitContrib tagger = GitUtils.parseGitContrib(pieces[i++]);
			builder.tagger(tagger);
		}
		
		void onBlockFinish() {
			builder.subject(contents.get(0));
			builder.body(Joiner.on("\n").join(contents.subList(1, contents.size())));
			Tag tag = builder.build();
			tags.put(tag.getName(), tag);
			
			// reset
			builder = Tag.builder();
			contents = Lists.newArrayList();
		}
	}
	
	
	@Override
	protected String getFormat() {
		return FORMAT;
	}

}
