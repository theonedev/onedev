package com.pmease.gitop.web.git.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.LineProcessor;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;
import com.pmease.gitop.web.git.GitUtils;

public class ListTagCommand extends GitCommand<Map<String, Commit>> {

	public ListTagCommand(File repoDir) {
		super(repoDir);
	}

	final static String LINE_SEPARATOR = "----g----i----t----o----p----";
	
	static final String FORMAT = "%d|%H|%aN|%aE|%ad|%cN|%cE|%cd%n%s%n%b%n" + LINE_SEPARATOR;
	
	@Override
	public Map<String, Commit> call() {
		Commandline cmd = cmd();
		cmd.addArgs("log", "-q", "--no-walk", "--date-order", "--tags", "--decorate=short", "--date=raw");
		cmd.addArgs("--format=" + FORMAT + "");
		
		TagConsumer consumer = new TagConsumer();
		cmd.execute(consumer , new LineConsumer.ErrorLogger());
		
		return consumer.commits;
	}
	
	static enum State {
		START, META, SUBJECT, BODY, SEPARATOR;
		
		public State next() {
			int i = ordinal() + 1;
			if (i >= State.values().length) {
				i = 0;
			}
			return State.values()[i];
		}
	}
	
	
	public static class TagConsumer extends LineConsumer implements LineProcessor<Map<String, Commit>> {

		private Commit.Builder currentBuilder;
		private List<String> messageLines = Lists.newLinkedList();
		private State state = State.START;
		private List<String> currentNames = Lists.newArrayList();
		
		final Map<String, Commit> commits = Maps.newLinkedHashMap();
		
		private void onMeta(String line) {
			String[] pieces = Iterables.toArray(Splitter.on("|").split(line), String.class);
			int i = 0;
			currentNames = parseTagNames(pieces[i++]);
			currentBuilder.hash(pieces[i++]);
			currentBuilder.author(com.pmease.commons.git.GitUtils.newPersonIdent(
					pieces[i++], GitUtils.parseEmail(pieces[i++]), GitUtils.parseRawDate(pieces[i++])));
			currentBuilder.committer(com.pmease.commons.git.GitUtils.newPersonIdent(
					pieces[i++], GitUtils.parseEmail(pieces[i++]), GitUtils.parseRawDate(pieces[i++])));
		}
		
		private void onFinishBlock() {
			// process previous
			String body = Joiner.on("\n").join(messageLines);
			currentBuilder.body(body);
			
			Commit commit = currentBuilder.build();
			for (String each : currentNames) {
				commits.put(each, commit);
			}
			
			currentBuilder = Commit.builder();
			messageLines = Lists.newLinkedList();
		}
		
		@Override
		public void consume(String line) {
			if (state == State.START) {
				if (line.trim().startsWith("(tag: ")) {
					state = State.META;
					currentBuilder = Commit.builder();
				} else
					return;
			}
			
			if (Objects.equal(line, LINE_SEPARATOR)) {
				state = State.SEPARATOR;
			}
			
			switch (state) {
			case META:
				onMeta(line);
				break;
				
			case SUBJECT:
				currentBuilder.subject(line);
				break;
				
			case BODY:
				messageLines.add(line);
				return; // don't transform state, we may have multiple lines here

			case SEPARATOR:
				onFinishBlock();
				break;
				
			default:
				return;
			}
			
			state = state.next();
		}

		@Override
		public boolean processLine(String line) throws IOException {
			consume(line);
			return true;
		}

		@Override
		public Map<String, Commit> getResult() {
			return commits;
		}
	}
	
	static final Pattern pTag = Pattern.compile("\\tag: (.*)");
	
	static List<String> parseTagNames(final String name) {
		String trimed = name.trim();
		if (!trimed.startsWith("(tag: "))
			throw new IllegalStateException("Tag name field: " + name);
		
		trimed = trimed.substring(1, trimed.length() - 1);
		String[] pieces = Iterables.toArray(Splitter.on(",").trimResults().omitEmptyStrings().split(trimed), String.class);
		List<String> result = Lists.newArrayList();
		for (String each : pieces) {
			result.add(each.substring("tag: ".length()));
		}
		
		return result;
	}
}
