package com.pmease.gitop.web.git.command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.util.RawParseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.pmease.commons.git.GitConfig;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.web.git.RepositoryException;
import com.pmease.gitop.web.util.UniversalEncodingDetector;

public class CatFileCommand implements Callable<String> {

	private final File workingDir;
	
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
		this.workingDir = dir;
	}
	
	public CatFileCommand revision(String revision) {
		this.revision = revision;
		return this;
	}
	
	public CatFileCommand path(String path) {
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
	
	static class ErrorOutputStream extends LogOutputStream {
		final static Logger log = LoggerFactory.getLogger(ErrorOutputStream.class);
		
		@Override
		protected void processLine(String line, int level) {
			log.error(line);
		}
	}
	
	static class LineOutputStream extends LogOutputStream {
		List<String> lines = Lists.newArrayList();

		@Override
		protected void processLine(String line, int level) {
			lines.add(line);
		}
		
		public Iterable<String> getLines() {
			return lines;
		}
		
		public String getOutput() {
			return Joiner.on("\n").join(lines);
		}
	}
	
	@Override
	public String call() {
		CommandLine cmd = new CommandLine(getGitExe());
		cmd.addArguments(new String[] { "cat-file", showType.arg, revision+":"+path });
		
		DefaultExecutor executor = new DefaultExecutor();
		executor.setWorkingDirectory(workingDir);
		
		ByteArrayOutputStream out = (ByteArrayOutputStream) getOutputStream();
		OutputStream err = getErrorStream();
		InputStream in = getInputStream();
		
		PumpStreamHandler streamHandler = new PumpStreamHandler(out, err, in);
		executor.setStreamHandler(streamHandler);
		
		try {
			int retCode = executor.execute(cmd);
			if (executor.isFailure(retCode)) {
				throw new RepositoryException("Executing command {" + cmd + "} failed with return code " + retCode);
			}
			
			byte[] bytes = out.toByteArray();
			return RawParseUtils.decode(UniversalEncodingDetector.detect(bytes), bytes);
		} catch (ExecuteException e) {
			throw Throwables.propagate(e);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(err);
			IOUtils.closeQuietly(in);
		}
	}
	
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream(1024);
	}
	
	protected OutputStream getErrorStream() {
		return new ErrorOutputStream();
	}
	
	protected InputStream getInputStream() {
		return null;
	}
	
	protected String getGitExe() {
		if (AppLoader.injector == null) {
			return "/usr/local/bin/git";
		} else {
			return AppLoader.getInstance(GitConfig.class).getExecutable();
		}
	}
}
