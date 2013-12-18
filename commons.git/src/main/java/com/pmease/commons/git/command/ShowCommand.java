package com.pmease.commons.git.command;

import java.io.ByteArrayOutputStream;
import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class ShowCommand extends GitCommand<byte[]> {

	private String revision;
	
	private String path;
	
	public ShowCommand(final File repoDir) {
		super(repoDir);
	}

	public ShowCommand revision(String revision) {
		this.revision = revision;
		return this;
	}
	
	public ShowCommand path(String path) {
		this.path = path;
		return this;
	}
	
	@Override
	public byte[] call() {
		Preconditions.checkNotNull(revision, "revision has to be specified for browse.");
		Preconditions.checkNotNull(path, "path has to be specified.");
		
		Commandline cmd = cmd().addArgs("show", revision + ":" + path);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		cmd.execute(baos, errorLogger).checkReturnCode();
		return baos.toByteArray();
	}

}
