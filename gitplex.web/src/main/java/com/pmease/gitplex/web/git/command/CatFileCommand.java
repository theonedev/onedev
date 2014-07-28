package com.pmease.gitplex.web.git.command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jgit.util.RawParseUtils;

import com.google.common.base.Throwables;
import com.pmease.commons.git.GitConfig;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.Charsets;
import com.pmease.commons.util.execution.Commandline;

public class CatFileCommand extends GitCommand<String> {

	private String revision;
	private String path;
	private ShowType showType = ShowType.PRETTY;
	
	static enum ShowType {
		TYPE("-t"), SIZE("-s"), PRETTY("-p");
		
		final String arg;
		
		ShowType(String arg) {
			this.arg = arg;
		}
	}
	
	public CatFileCommand(File dir) {
		super(dir);
	}
	
	public CatFileCommand object(String revision, String path) {
		this.revision = revision;
		this.path = path;
		return this;
	}

	public CatFileCommand showSize() {
		this.showType = ShowType.SIZE;
		return this;
	}
	
	public CatFileCommand showType() {
		this.showType = ShowType.TYPE;
		return this;
	}
	
	public CatFileCommand prettyPrint() {
		this.showType = ShowType.PRETTY;
		return this;
	}
	
	@Override
	public String call() {
		Commandline cmd = cmd();
		cmd.addArgs("cat-file", showType.arg, revision+":"+path);
		
		try (
				ByteArrayOutputStream out = (ByteArrayOutputStream) getOutputStream();
				InputStream in = getInputStream()) {
			
			cmd.execute(out, errorLogger).checkReturnCode();
			
			byte[] bytes = out.toByteArray();
			if (showType == ShowType.PRETTY) {
				return RawParseUtils.decode(Charsets.detectFrom(bytes), bytes);
			} else {
				// needn't detect charset when showing size or type
				return RawParseUtils.decode(bytes);
			}
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
	
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream(1024);
	}
	
	protected InputStream getInputStream() {
		return null;
	}
	
	@Override
	protected String getGitExe() {
		if (AppLoader.injector == null) {
			return "git";
		} else {
			return AppLoader.getInstance(GitConfig.class).getExecutable();
		}
	}
}
