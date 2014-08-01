package com.pmease.gitplex.web.git.command;

import java.io.File;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.parboiled.common.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ArchiveCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(ArchiveCommand.class);
	
	public static enum Format {
		TAR("tar"), TGZ("tgz"), TAR_GZ("tar.gz"), ZIP("zip");
		
		private final String suffix;
		
		Format(String suffix) {
			this.suffix = suffix;
		}
		
		public String getSuffix() {
			return suffix;
		}
	}
	
	private Format format = Format.ZIP;
	private List<String> paths = Lists.newArrayList();
	private String treeish;
	private String prefix;
	private String outputFile;
	
	private final OutputStream output;
	
	public ArchiveCommand(File repoDir, OutputStream output) {
		super(repoDir);
		this.output = output;
	}

	public ArchiveCommand format(Format format) {
		this.format = format;
		return this;
	}
	
	public ArchiveCommand path(String path) {
		this.paths = Collections.singletonList(path);
		return this;
	}
	
	public ArchiveCommand paths(List<String> paths) {
		this.paths = paths;
		return this;
	}
	
	public ArchiveCommand treeish(String treeish) {
		this.treeish = treeish;
		return this;
	}
	
	public ArchiveCommand prefix(String prefix) {
		this.prefix = prefix;
		return this;
	}
	
	public ArchiveCommand outputFile(String outputFile) {
		this.outputFile = outputFile;
		return this;
	}
	
	@Override
	public Void call() {
		Commandline cmd = cmd();
		
		cmd.addArgs("archive");
		
		applyArgs(cmd);
		
		cmd.execute(output, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		});
		
		return null;
	}
	
	protected void applyArgs(Commandline cmd) {
		cmd.addArgs("--format", format.suffix);
		if (!Strings.isNullOrEmpty(prefix)) {
			cmd.addArgs("--prefix=" + prefix);
		}
		
		if (!Strings.isNullOrEmpty(outputFile)) {
			cmd.addArgs("-o", outputFile);
		}
		
		cmd.addArgs(Preconditions.checkArgNotNull(treeish, "treeish"));
		
		if (!paths.isEmpty()) {
			for (String each : paths) {
				cmd.addArgs(each);
			}
		}
	}
}
