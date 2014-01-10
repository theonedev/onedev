package com.pmease.gitop.web.git.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.gitop.web.page.project.source.commit.patch.Patch;

public class DiffTreeCommand extends AbstractDiffCommand<Patch> {

	boolean recurse;
	
	public DiffTreeCommand(File repoDir) {
		super(repoDir);
	}

	@Override
	protected Commandline newCommand() {
		Commandline cmd = super.newCommand();
		cmd.addArgs("--root");
		cmd.addArgs("--no-commit-id");
		
		if (recurse) {
			cmd.addArgs("-r");
		}
		
		return cmd;
	}
	
	@Override
	protected void applyPaths(Commandline cmd) {
		cmd.addArgs("--");
		for (String path : paths) {
			if (!Strings.isNullOrEmpty(path)) {
				cmd.addArgs(path);
			}
		}
	}
	
	public DiffTreeCommand recurse(boolean recurse) {
		this.recurse = recurse;
		return this;
	}
	
	@Override
	public Patch call() {
		Commandline cmd = buildCommand();
		
		final Patch patch = new Patch();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
		ByteArrayInputStream in = null;
		
		try {
			cmd.execute(out, errorLogger);
			in = ByteStreams.newInputStreamSupplier(out.toByteArray()).getInput();
			patch.parse(in);
			return patch;
		} catch (IOException e) {
			throw Throwables.propagate(e);
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}		
	}

	@Override
	protected String getSubCommand() {
		return "diff-tree";
	}
}
