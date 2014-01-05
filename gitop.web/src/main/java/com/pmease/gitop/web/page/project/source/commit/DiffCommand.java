package com.pmease.gitop.web.page.project.source.commit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.gitop.web.page.project.source.commit.patch.Patch;

public class DiffCommand extends GitCommand<Patch> {

	private String fromRev;
	
	private String toRev;
	
	private String path;
	
	private int contextLines;
	
	public DiffCommand(File repoDir) {
		super(repoDir);
	}

	public DiffCommand fromRev(String fromRev) {
		this.fromRev = fromRev;
		return this;
	}
	
	public DiffCommand toRev(String toRev) {
		this.toRev = toRev;
		return this;
	}

	public DiffCommand path(String path) {
		this.path = path;
		return this;
	}
	
	public DiffCommand contextLines(int contextLines) {
		this.contextLines = contextLines;
		return this;
	}
	
	@Override
	protected String getGitExe() {
		if (AppLoader.injector == null) {
			return "/usr/local/bin/git";
		} else {
			return super.getGitExe();
		}
	}
	
	@Override
	public Patch call() {
		Commandline cmd = cmd();
		
		cmd.addArgs("diff", fromRev + ".." + toRev, "--full-index", "-p", "--no-color", "--find-renames");
		
		if (contextLines != 0)
			cmd.addArgs("--unified=" + contextLines);
		if (path != null)
			cmd.addArgs("--", path);
		
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
}
